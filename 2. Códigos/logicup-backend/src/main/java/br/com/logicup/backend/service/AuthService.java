package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.AutenticacaoResultado;
import br.com.logicup.backend.dto.CadastroPendenteResponse;
import br.com.logicup.backend.dto.CadastroRequest;
import br.com.logicup.backend.dto.LoginRequest;
import br.com.logicup.backend.dto.PerfilCadastro;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.exception.CredenciaisInvalidasException;
import br.com.logicup.backend.email.EmailDeContaSolicitado;
import br.com.logicup.backend.email.TipoEmailConta;
import br.com.logicup.backend.exception.EmailJaCadastradoException;
import br.com.logicup.backend.exception.EmailNaoConfirmadoException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.exception.UsernameEmUsoException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.FinalidadeToken;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.ProtecaoForcaBruta;
import br.com.logicup.backend.security.TokenService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Regras de negócio de autenticação: cadastro, login e "quem sou eu".
 * O Controller só recebe/devolve HTTP; as decisões ficam aqui.
 *
 * Sprint 10: confirmação de e-mail (decisão C: sem confirmar, não entra), reenvio do link,
 * "esqueci minha senha" e e-mail de boas-vindas. Os e-mails são EVENTOS publicados aqui e enviados
 * depois do commit pelo EnvioDeEmailListener (decisão E1-A).
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AceiteService aceiteService;
    private final ConquistaService conquistaService;
    private final ProtecaoForcaBruta protecao; // Sprint 9: limite de tentativas (login e cadastro)
    private final TokenUsuarioService tokens;   // Sprint 10: códigos de uso único dos links
    private final ApplicationEventPublisher eventos; // Sprint 10: "mande este e-mail" (depois do commit)
    private final Clock clock;

    /*
     * Hash de uma senha qualquer, usado quando o e-mail NÃO existe no login.
     * Assim o BCrypt roda nos dois casos e o tempo de resposta fica parecido —
     * sem isso, dava para descobrir e-mails cadastrados medindo o tempo (reforça a AU7).
     */
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService, AceiteService aceiteService,
                       ConquistaService conquistaService, ProtecaoForcaBruta protecao,
                       TokenUsuarioService tokens, ApplicationEventPublisher eventos, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.aceiteService = aceiteService;
        this.conquistaService = conquistaService;
        this.protecao = protecao;
        this.tokens = tokens;
        this.eventos = eventos;
        this.clock = clock;
        this.hashFicticio = passwordEncoder.encode("logicup-senha-ficticia-para-tempo-constante");
    }

    /** "Ana@X.com " e "ana@x.com" são o mesmo e-mail (C4). */
    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * @param ip endereço de quem pede (vem do Controller), usado só no limite de tentativas (D4)
     * @return Sprint 10: a conta nasce com o e-mail NÃO confirmado e a pessoa NÃO fica logada (antes era C1).
     *         O link de confirmação sai por e-mail depois do commit.
     */
    @Transactional
    public CadastroPendenteResponse cadastrar(CadastroRequest dados, String ip) {
        protecao.verificarERegistrarCadastro(ip); // Sprint 9 (D4): limite de cadastros por hora por IP (60 desde a Sprint 10)


        // Sprint 8: o aceite ATIVO (aceiteTermos = true) já foi exigido pelo @Valid; aqui, a VERSÃO lida
        aceiteService.exigirVersoesVigentes(dados.versaoTermos(), dados.versaoPrivacidade());

        String email = normalizarEmail(dados.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }
        // Sprint 7: checagem "amigável" (mensagem no campo). A garantia final é a UNIQUE do banco:
        // se dois cadastros com o mesmo username chegarem juntos, o banco recusa o segundo (409 pelo handler).
        if (usuarioRepository.existsByUsername(dados.username())) {
            throw new UsernameEmUsoException();
        }

        String nome = dados.nome().trim();
        String hash = passwordEncoder.encode(dados.senha()); // a senha em texto puro nunca é gravada

        // ALUNO ganha também a linha na tabela aluno (XP, streak, nível); PROFESSOR é só Usuario
        Usuario novo = (dados.perfil() == PerfilCadastro.ALUNO)
                ? new Aluno(nome, dados.username(), email, hash)
                : new Usuario(nome, dados.username(), email, hash, dados.perfil().paraTipoUsuario());

        Usuario salvo = usuarioRepository.save(novo);
        aceiteService.registrarVersoesVigentes(salvo); // mesma transação: sem aceite gravado, não há conta
        solicitarEmail(TipoEmailConta.CONFIRMAR_EMAIL, salvo, tokens.gerar(salvo, FinalidadeToken.CONFIRMAR_EMAIL));
        return CadastroPendenteResponse.para(email);
    }

    /**
     * Sprint 7: login com e-mail OU username no mesmo campo.
     * Tem "@" -> é e-mail (username não aceita "@"); senão -> é username. Os dois são normalizados
     * (minúsculas, sem espaços nas pontas), então "Ana.Silva" e "ana.silva" entram na mesma conta.
     *
     * Sprint 9 (B1): deixou de ser readOnly porque o login de um ALUNO também avalia as conquistas.
     * É assim que quem já tinha progresso antes desta versão ganha as medalhas (retroativo, decisão M5).
     */
    @Transactional
    public AutenticacaoResultado login(LoginRequest dados, String ip) {
        // a MESMA normalização da busca: "Ana@X.com" e "ana@x.com" contam como a mesma conta no limite
        String login = dados.ehEmail() ? normalizarEmail(dados.login()) : CadastroRequest.normalizarUsername(dados.login());

        // Sprint 9 (D1): bloqueado -> 429 ANTES do BCrypt (nem gasta processamento; e nem a senha certa entra).
        // Sprint 10: a tentativa já é contada aqui, de forma atômica (sem brecha para tentativas simultâneas).
        protecao.reservarTentativaDeLogin(login, ip);

        Optional<Usuario> usuario = dados.ehEmail()
                ? usuarioRepository.findByEmail(login)
                : usuarioRepository.findByUsername(login);

        // Sprint 10: conta só do Google (senha NULL) também compara com o hash fictício: mesmo tempo, mesmo 401
        String hashParaComparar = usuario.map(Usuario::getSenha).filter(Objects::nonNull).orElse(hashFicticio);
        boolean senhaConfere = passwordEncoder.matches(dados.senha(), hashParaComparar)
                && usuario.map(Usuario::temSenha).orElse(false);

        if (usuario.isEmpty() || !senhaConfere) {
            // o erro já foi contado na reserva, mesmo se a conta não existe: o limite não revela quem está cadastrado
            throw new CredenciaisInvalidasException(); // mesma mensagem em todos os casos (AU7)
        }
        protecao.registrarLoginCerto(login, ip); // senha certa: devolve a tentativa reservada
        // Sprint 10 (decisão C): senha certa, mas e-mail não confirmado -> 403 EMAIL_NAO_CONFIRMADO.
        // Só quem ACERTOU a senha recebe essa resposta, então ela não revela nada a quem está chutando.
        // O frontend mostra o botão "reenviar link" (por isso a exceção leva o e-mail).
        if (!usuario.get().isEmailConfirmado()) {
            throw new EmailNaoConfirmadoException(usuario.get().getEmail());
        }
        // com a herança JOINED, o Hibernate já devolve um Aluno quando a pessoa é aluno
        if (usuario.get() instanceof Aluno aluno) {
            conquistaService.avaliar(aluno);
        }
        // Sprint 8: o banco diz se já aceitou a versão vigente; a resposta vai DENTRO do token
        return autenticado(usuario.get(), aceiteService.aceitouVersoesVigentes(usuario.get().getId()));
    }

    // ---------------- Sprint 10: confirmação de e-mail e nova senha ----------------

    /**
     * Abre o link do e-mail de confirmação: confirma o e-mail e JÁ DEIXA A PESSOA LOGADA
     * (ela acabou de provar que é dona do e-mail; pedir a senha de novo seria só atrito).
     * Na primeira confirmação, também pede o e-mail de boas-vindas (decisão D1-A).
     *
     * @throws br.com.logicup.backend.exception.LinkInvalidoException código errado, vencido ou já usado
     */
    @Transactional
    public AutenticacaoResultado confirmarEmail(String codigo) {
        Usuario usuario = tokens.consumir(codigo, FinalidadeToken.CONFIRMAR_EMAIL);
        confirmarSeAindaNaoConfirmado(usuario);
        if (usuario instanceof Aluno aluno) {
            conquistaService.avaliar(aluno); // o mesmo que o login faz
        }
        return autenticado(usuario, aceiteService.aceitouVersoesVigentes(usuario.getId()));
    }

    /**
     * "Não recebi o e-mail". A resposta é SEMPRE a mesma (o Controller devolve 202), exista ou não a conta,
     * esteja ou não confirmada: senão este endpoint viraria um jeito de descobrir quem está cadastrado.
     * O e-mail só sai de verdade para uma conta que existe e ainda não confirmou.
     */
    @Transactional
    public void reenviarConfirmacao(String emailInformado, String ip) {
        String email = normalizarEmail(emailInformado);
        protecao.verificarERegistrarPedidoDeEmail("confirmacao", email, ip); // 3/h por e-mail, 60/h por IP
        usuarioRepository.findByEmail(email)
                .filter(usuario -> !usuario.isEmailConfirmado())
                .ifPresent(usuario -> solicitarEmail(TipoEmailConta.CONFIRMAR_EMAIL, usuario,
                        tokens.gerar(usuario, FinalidadeToken.CONFIRMAR_EMAIL)));
    }

    /**
     * "Esqueci minha senha". Mesma ideia do reenvio: resposta sempre igual; o e-mail só sai se a conta existe.
     * Vale também para conta só do Google (é assim que ela cria uma senha própria) e para conta não
     * confirmada (o link prova que a pessoa é dona do e-mail).
     */
    @Transactional
    public void esqueciSenha(String emailInformado, String ip) {
        String email = normalizarEmail(emailInformado);
        protecao.verificarERegistrarPedidoDeEmail("senha", email, ip);
        usuarioRepository.findByEmail(email)
                .ifPresent(usuario -> solicitarEmail(TipoEmailConta.REDEFINIR_SENHA, usuario,
                        tokens.gerar(usuario, FinalidadeToken.REDEFINIR_SENHA)));
    }

    /**
     * Abre o link de "esqueci minha senha" e grava a senha nova (só o hash BCrypt).
     * Como o link chegou no e-mail, ele também CONFIRMA o e-mail, se ainda não estava confirmado.
     * Não faz login automático: a pessoa entra com a senha nova (assim ela confirma que guardou certo).
     */
    @Transactional
    public void redefinirSenha(String codigo, String novaSenha) {
        Usuario usuario = tokens.consumir(codigo, FinalidadeToken.REDEFINIR_SENHA);
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        confirmarSeAindaNaoConfirmado(usuario);
    }

    private void confirmarSeAindaNaoConfirmado(Usuario usuario) {
        if (!usuario.isEmailConfirmado()) {
            usuario.confirmarEmail(LocalDateTime.now(clock)); // o Hibernate grava no commit (entidade gerenciada)
            solicitarEmail(TipoEmailConta.BOAS_VINDAS, usuario, null);
        }
    }

    /** Só ANUNCIA o pedido; o envio acontece depois do commit, em outra thread (EnvioDeEmailListener). */
    private void solicitarEmail(TipoEmailConta tipo, Usuario usuario, String codigo) {
        eventos.publishEvent(new EmailDeContaSolicitado(tipo, usuario.getEmail(), usuario.getNome(), codigo));
    }

    /**
     * aceitePendente vem do TOKEN da sessão (e não do banco), para bater com o que o
     * AceitePendenteFilter faz: se o token não tem o aceite vigente, a janela de aceite aparece.
     */
    @Transactional(readOnly = true)
    public UsuarioResponse buscarUsuarioLogado(Integer idUsuario, boolean aceitePendente) {
        return usuarioRepository.findById(idUsuario)
                .map(usuario -> UsuarioResponse.de(usuario, aceitePendente))
                .orElseThrow(SessaoInvalidaException::new);
    }

    private AutenticacaoResultado autenticado(Usuario usuario, boolean aceitouVersoesVigentes) {
        return new AutenticacaoResultado(UsuarioResponse.de(usuario, !aceitouVersoesVigentes),
                tokenService.gerarToken(usuario, aceitouVersoesVigentes));
    }
}
