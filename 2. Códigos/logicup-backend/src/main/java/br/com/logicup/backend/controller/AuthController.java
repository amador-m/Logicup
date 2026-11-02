package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.AutenticacaoResultado;
import br.com.logicup.backend.dto.CadastroRequest;
import br.com.logicup.backend.dto.LoginRequest;
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

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroRequest dados,
                                                     HttpServletRequest request, HttpServletResponse response) {
        AutenticacaoResultado resultado = authService.cadastrar(dados, request.getRemoteAddr()); // se falhar, nada abaixo acontece
        trocarTokenCsrf(request, response);
        return comCookieDeLogin(HttpStatus.CREATED, resultado, response);
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest dados,
                                                 HttpServletRequest request, HttpServletResponse response) {
        // Sprint 9: o IP vai para o limite de tentativas. getRemoteAddr() = quem abriu a conexão com o servidor.
        // ATENÇÃO (deploy): atrás de um proxy, isso seria o IP do PROXY; lá configuramos o Spring para ler
        // o IP real do X-Forwarded-For, aceitando esse cabeçalho só do proxy confiável.
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
