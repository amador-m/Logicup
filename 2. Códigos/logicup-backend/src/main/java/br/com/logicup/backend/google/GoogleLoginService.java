package br.com.logicup.backend.google;

import br.com.logicup.backend.dto.AutenticacaoResultado;
import br.com.logicup.backend.dto.CompletarCadastroGoogleRequest;
import br.com.logicup.backend.dto.PerfilCadastro;
import br.com.logicup.backend.email.EmailDeContaSolicitado;
import br.com.logicup.backend.email.TipoEmailConta;
import br.com.logicup.backend.exception.EmailJaCadastradoException;
import br.com.logicup.backend.exception.UsernameEmUsoException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.ProtecaoForcaBruta;
import br.com.logicup.backend.service.AceiteService;
import br.com.logicup.backend.service.AuthService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Sprint 10: as REGRAS do "Entrar com Google" (decisões D2 a D5). Quem fala com o Google é o Spring Security;
 * este Service só recebe o resultado (IdentidadeGoogle) e decide o que fazer no LogiCup.
 *
 *  1. Já existe conta ligada a este Google (google_id = sub)  -> entra.
 *  2. Não, mas o E-MAIL já tem conta                            -> liga a conta ao Google e entra (D4).
 *       - se essa conta nunca confirmou o e-mail: confirma agora e APAGA a senha dela. Quem criou aquela
 *         senha não provou ser dono do e-mail (pode ter sido outra pessoa, de propósito);
 *       - se ela já estava ligada a OUTRA conta Google: recusa.
 *  3. Pessoa nova                                              -> cadastro pendente (D3): falta escolher nome
 *     de usuário e perfil e aceitar os Termos (completarCadastro).
 *  Em todos os casos: o Google precisa garantir que o e-mail é da pessoa (email_verified), senão recusa.
 */
@Service
public class GoogleLoginService {

    /** Resultado da volta do Google: ou já entrou, ou falta completar o cadastro. */
    public sealed interface Resultado permits Entrou, CadastroPendente {
    }

    public record Entrou(AutenticacaoResultado autenticacao) implements Resultado {
    }

    public record CadastroPendente(IdentidadeGoogle google) implements Resultado {
    }

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final AceiteService aceiteService;
    private final ProtecaoForcaBruta protecao;
    private final ApplicationEventPublisher eventos;
    private final Clock clock;

    public GoogleLoginService(UsuarioRepository usuarioRepository, AuthService authService, AceiteService aceiteService,
                              ProtecaoForcaBruta protecao, ApplicationEventPublisher eventos, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.aceiteService = aceiteService;
        this.protecao = protecao;
        this.eventos = eventos;
        this.clock = clock;
    }

    @Transactional
    public Resultado entrar(IdentidadeGoogle google) {
        if (!google.emailVerificado()) {
            throw new LoginGoogleRecusadoException(LoginGoogleRecusadoException.Motivo.EMAIL_NAO_VERIFICADO);
        }

        // 1) já ligada: entra (mesmo que a pessoa tenha trocado o e-mail no Google; o "sub" não muda)
        Optional<Usuario> ligada = usuarioRepository.findByGoogleId(google.sub());
        if (ligada.isPresent()) {
            Usuario usuario = ligada.get();
            usuario.confirmarEmail(LocalDateTime.now(clock)); // idempotente (já estava confirmada)
            return new Entrou(authService.entrarNaConta(usuario));
        }

        // 2) mesmo e-mail, conta ainda sem Google: liga (D4)
        Optional<Usuario> pelaConta = usuarioRepository.findByEmail(AuthService.normalizarEmail(google.email()));
        if (pelaConta.isPresent()) {
            Usuario usuario = pelaConta.get();
            if (usuario.getGoogleId() != null) {
                throw new LoginGoogleRecusadoException(LoginGoogleRecusadoException.Motivo.CONTA_GOOGLE_DIFERENTE);
            }
            if (!usuario.isEmailConfirmado()) {
                usuario.descartarSenha();          // a senha foi criada por quem NÃO provou ser dono do e-mail
                usuario.confirmarEmail(LocalDateTime.now(clock));
                pedirBoasVindas(usuario);
            }
            usuario.ligarAoGoogle(google.sub());
            return new Entrou(authService.entrarNaConta(usuario));
        }

        // 3) pessoa nova
        return new CadastroPendente(google);
    }

    /**
     * Cria a conta de quem veio do Google pela 1ª vez. Mesmas regras do cadastro normal (limite por IP,
     * versão vigente dos Termos, username único), mas sem senha e com o e-mail JÁ confirmado (o Google garantiu).
     */
    @Transactional
    public AutenticacaoResultado completarCadastro(IdentidadeGoogle google, CompletarCadastroGoogleRequest dados,
                                                   String ip) {
        protecao.verificarERegistrarCadastro(ip);
        aceiteService.exigirVersoesVigentes(dados.versaoTermos(), dados.versaoPrivacidade());

        String email = AuthService.normalizarEmail(google.email());
        // alguém pode ter criado a conta nesses minutos (pelo cadastro normal ou por outra aba)
        if (usuarioRepository.existsByEmail(email) || usuarioRepository.findByGoogleId(google.sub()).isPresent()) {
            throw new EmailJaCadastradoException();
        }
        if (usuarioRepository.existsByUsername(dados.username())) {
            throw new UsernameEmUsoException();
        }

        Usuario novo = (dados.perfil() == PerfilCadastro.ALUNO)
                ? new Aluno(dados.nome(), dados.username(), email, null)
                : new Usuario(dados.nome(), dados.username(), email, null, dados.perfil().paraTipoUsuario());
        novo.ligarAoGoogle(google.sub());
        novo.confirmarEmail(LocalDateTime.now(clock));

        Usuario salvo = usuarioRepository.save(novo);
        aceiteService.registrarVersoesVigentes(salvo);
        pedirBoasVindas(salvo);
        return authService.entrarNaConta(salvo);
    }

    private void pedirBoasVindas(Usuario usuario) {
        eventos.publishEvent(new EmailDeContaSolicitado(TipoEmailConta.BOAS_VINDAS, usuario.getEmail(),
                usuario.getNome(), null));
    }
}
