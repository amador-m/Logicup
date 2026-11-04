package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.AutenticacaoResultado;
import br.com.logicup.backend.dto.CadastroPendenteResponse;
import br.com.logicup.backend.dto.CadastroRequest;
import br.com.logicup.backend.dto.ConfirmarEmailRequest;
import br.com.logicup.backend.dto.EmailRequest;
import br.com.logicup.backend.dto.LoginRequest;
import br.com.logicup.backend.dto.RedefinirSenhaRequest;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.security.CookiesLegados;
import br.com.logicup.backend.security.TokenService;
import br.com.logicup.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de autenticação. O Controller cuida só do "HTTP":
 * recebe o JSON, valida (@Valid), chama o Service e monta a resposta (status, cookie, corpo).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final CsrfTokenRepository csrfTokenRepository;

    public AuthController(AuthService authService, TokenService tokenService, CsrfTokenRepository csrfTokenRepository) {
        this.authService = authService;
        this.tokenService = tokenService;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    /**
     * O React chama ao abrir o site. Pedir o CsrfToken faz o Spring gerar o cookie __Host-XSRF-TOKEN,
     * que o frontend lê e devolve no cabeçalho X-XSRF-TOKEN em todo POST.
     * Tarefa 6: aproveita para apagar os cookies com os nomes antigos (CookiesLegados).
     */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken, HttpServletResponse response) {
        csrfToken.getToken(); // "toca" no token para garantir que o cookie seja criado
        adicionarCookies(response, CookiesLegados.paraApagar());
        return ResponseEntity.noContent().build();
    }

    /**
     * Sprint 10: o cadastro cria a conta (201) mas NÃO faz login: não há cookie de sessão na resposta.
     * A pessoa entra quando abrir o link de confirmação (POST /confirmar-email).
     */
    @PostMapping("/cadastro")
    public ResponseEntity<CadastroPendenteResponse> cadastrar(@Valid @RequestBody CadastroRequest dados,
                                                              HttpServletRequest request) {
        CadastroPendenteResponse pendente = authService.cadastrar(dados, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(pendente);
    }

    /** Sprint 10: o React lê o código do link (depois do "#") e manda aqui. Deu certo -> já sai logado. */
    @PostMapping("/confirmar-email")
    public ResponseEntity<UsuarioResponse> confirmarEmail(@Valid @RequestBody ConfirmarEmailRequest dados,
                                                          HttpServletRequest request, HttpServletResponse response) {
        AutenticacaoResultado resultado = authService.confirmarEmail(dados.token()); // link ruim -> 400 LINK_INVALIDO
        trocarTokenCsrf(request, response);
        return comCookieDeLogin(HttpStatus.OK, resultado, response);
    }

    /**
     * Sprint 10: 202 Accepted = "pedido recebido". É a MESMA resposta para e-mail cadastrado ou não
     * (não dá para usar este endpoint para descobrir quem tem conta).
     */
    @PostMapping("/reenviar-confirmacao")
    public ResponseEntity<Void> reenviarConfirmacao(@Valid @RequestBody EmailRequest dados, HttpServletRequest request) {
        authService.reenviarConfirmacao(dados.email(), request.getRemoteAddr());
        return ResponseEntity.accepted().build();
    }

    /** Sprint 10: "Esqueci minha senha". Mesma resposta (202) exista ou não a conta. */
    @PostMapping("/esqueci-senha")
    public ResponseEntity<Void> esqueciSenha(@Valid @RequestBody EmailRequest dados, HttpServletRequest request) {
        authService.esqueciSenha(dados.email(), request.getRemoteAddr());
        return ResponseEntity.accepted().build();
    }

    /** Sprint 10: grava a senha nova. 204 = deu certo; a pessoa entra pelo login com a senha nova. */
    @PostMapping("/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest dados) {
        authService.redefinirSenha(dados.token(), dados.novaSenha());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest dados,
                                                 HttpServletRequest request, HttpServletResponse response) {
        // Sprint 9: o IP vai para o limite de tentativas. getRemoteAddr() = quem abriu a conexão com o servidor.
        // Em produção (atrás do Caddy), o Tomcat já troca este valor pelo IP real do aluno, lido do
        // X-Forwarded-For e aceito SÓ quando vem do proxy confiável (application-prod.properties).
        AutenticacaoResultado resultado = authService.login(dados, request.getRemoteAddr()); // senha errada -> 401
        trocarTokenCsrf(request, response);
        return comCookieDeLogin(HttpStatus.OK, resultado, response);
    }

    /**
     * O JavaScript não consegue apagar um cookie httpOnly: quem apaga é o servidor.
     * A remoção usa os MESMOS atributos da criação (TokenService.baseCookie), senão o navegador ignora.
     * O token CSRF é TROCADO (não apagado): o React precisa de um para o próximo login.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        trocarTokenCsrf(request, response);
        adicionarCookies(response, tokenService.criarCookieExpirado().toString());
        adicionarCookies(response, CookiesLegados.paraApagar());
        return ResponseEntity.noContent().build();
    }

    /**
     * Tarefa 6: gera um token CSRF NOVO quando a "sessão" muda (login, cadastro, logout).
     * Assim um token obtido antes do login (por exemplo, plantado por alguém) não continua valendo depois.
     * O Spring só faz isso sozinho no login por formulário, que o LogiCup não usa; por isso é explícito aqui.
     * O cookie sai com os atributos do repositório (__Host-, Secure, SameSite=Strict, Path=/).
     */
    private void trocarTokenCsrf(HttpServletRequest request, HttpServletResponse response) {
        csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response);
    }

    /** Quem está logado? O id vem do TOKEN (sub), nunca de um parâmetro enviado pelo cliente. */
    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        boolean aceitePendente = !VersoesLegais.aceiteVigente(jwt.getClaimAsString(TokenService.CLAIM_ACEITE));
        return authService.buscarUsuarioLogado(Integer.valueOf(jwt.getSubject()), aceitePendente);
    }

    private ResponseEntity<UsuarioResponse> comCookieDeLogin(HttpStatus status, AutenticacaoResultado resultado,
                                                             HttpServletResponse response) {
        adicionarCookies(response, tokenService.criarCookie(resultado.token()).toString());
        adicionarCookies(response, CookiesLegados.paraApagar());
        return ResponseEntity.status(status).body(resultado.usuario()); // o token vai SÓ no cookie
    }

    /**
     * Tarefa 6: TODO cookie desta classe é escrito por aqui, com addHeader (que ACRESCENTA).
     * Motivo: o repositório CSRF do Spring escreve o cookie dele direto na resposta. Se o ResponseEntity
     * também trouxesse um cabeçalho Set-Cookie, o Spring MVC SUBSTITUIRIA os Set-Cookie já escritos,
     * e o cookie CSRF novo sumiria da resposta (bug encontrado pelo CookiesSegurancaIntegrationTest).
     */
    private static void adicionarCookies(HttpServletResponse response, String... cookies) {
        for (String cookie : cookies) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie);
        }
    }
}
