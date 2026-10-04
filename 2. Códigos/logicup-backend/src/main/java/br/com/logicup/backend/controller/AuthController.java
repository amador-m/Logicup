package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.AutenticacaoResultado;
import br.com.logicup.backend.dto.CadastroRequest;
import br.com.logicup.backend.dto.LoginRequest;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.security.TokenService;
import br.com.logicup.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CsrfToken;
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

    public AuthController(AuthService authService, TokenService tokenService) {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    /**
     * O React chama ao abrir o site. Pedir o CsrfToken faz o Spring gerar o cookie XSRF-TOKEN,
     * que o frontend lê e devolve no cabeçalho X-XSRF-TOKEN em todo POST.
     */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken(); // "toca" no token para garantir que o cookie seja criado
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroRequest dados) {
        return comCookieDeLogin(HttpStatus.CREATED, authService.cadastrar(dados));
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest dados) {
        return comCookieDeLogin(HttpStatus.OK, authService.login(dados));
    }

    /** O JavaScript não consegue apagar um cookie httpOnly: quem apaga é o servidor. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, tokenService.criarCookieExpirado().toString())
                .build();
    }

    /** Quem está logado? O id vem do TOKEN (sub), nunca de um parâmetro enviado pelo cliente. */
    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return authService.buscarUsuarioLogado(Integer.valueOf(jwt.getSubject()));
    }

    private ResponseEntity<UsuarioResponse> comCookieDeLogin(HttpStatus status, AutenticacaoResultado resultado) {
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, tokenService.criarCookie(resultado.token()).toString())
                .body(resultado.usuario()); // o token vai SÓ no cookie
    }
}
