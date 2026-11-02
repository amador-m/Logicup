package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.AutenticacaoResultado;
import br.com.logicup.backend.dto.CadastroRequest;
import br.com.logicup.backend.dto.LoginRequest;
import br.com.logicup.backend.dto.PerfilCadastro;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.exception.CredenciaisInvalidasException;
import br.com.logicup.backend.exception.EmailJaCadastradoException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.exception.UsernameEmUsoException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.ProtecaoForcaBruta;
import br.com.logicup.backend.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Regras de negócio de autenticação: cadastro, login e "quem sou eu".
 * O Controller só recebe/devolve HTTP; as decisões ficam aqui.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AceiteService aceiteService;
    private final ConquistaService conquistaService;
    private final ProtecaoForcaBruta protecao; // Sprint 9: limite de tentativas (login e cadastro)

    /*
     * Hash de uma senha qualquer, usado quando o e-mail NÃO existe no login.
     * Assim o BCrypt roda nos dois casos e o tempo de resposta fica parecido —
     * sem isso, dava para descobrir e-mails cadastrados medindo o tempo (reforça a AU7).
     */
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService, AceiteService aceiteService,
                       ConquistaService conquistaService, ProtecaoForcaBruta protecao) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.aceiteService = aceiteService;
        this.conquistaService = conquistaService;
        this.protecao = protecao;
        this.hashFicticio = passwordEncoder.encode("logicup-senha-ficticia-para-tempo-constante");
    }

    /** "Ana@X.com " e "ana@x.com" são o mesmo e-mail (C4). */
    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** @param ip endereço de quem pede (vem do Controller), usado só no limite de tentativas (D4) */
    @Transactional
    public AutenticacaoResultado cadastrar(CadastroRequest dados, String ip) {
        protecao.verificarERegistrarCadastro(ip); // Sprint 9 (D4): 10 tentativas de cadastro por hora por IP


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
        return autenticado(salvo, true); // C1: cadastro já deixa a pessoa logada
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

        // Sprint 9 (D1): bloqueado -> 429 ANTES do BCrypt (nem gasta processamento; e nem a senha certa entra)
        protecao.verificarLogin(login, ip);

        Optional<Usuario> usuario = dados.ehEmail()
                ? usuarioRepository.findByEmail(login)
                : usuarioRepository.findByUsername(login);

        String hashParaComparar = usuario.map(Usuario::getSenha).orElse(hashFicticio);
        boolean senhaConfere = passwordEncoder.matches(dados.senha(), hashParaComparar);

        if (usuario.isEmpty() || !senhaConfere) {
            // conta o erro mesmo se a conta não existe: assim o limite não revela quem está cadastrado
            protecao.registrarErroDeLogin(login, ip);
            throw new CredenciaisInvalidasException(); // mesma mensagem em todos os casos (AU7)
        }
        protecao.registrarLoginCerto(login, ip);
        // com a herança JOINED, o Hibernate já devolve um Aluno quando a pessoa é aluno
        if (usuario.get() instanceof Aluno aluno) {
            conquistaService.avaliar(aluno);
        }
        // Sprint 8: o banco diz se já aceitou a versão vigente; a resposta vai DENTRO do token
        return autenticado(usuario.get(), aceiteService.aceitouVersoesVigentes(usuario.get().getId()));
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
