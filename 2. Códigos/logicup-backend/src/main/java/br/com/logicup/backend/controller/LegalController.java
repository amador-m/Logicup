package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.AceiteRequest;
import br.com.logicup.backend.dto.AutenticacaoResultado;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.security.TokenService;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.AceiteService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 8: aceite dos Termos de Uso e ciência da Política de Privacidade por quem JÁ tem conta
 * (no cadastro, o aceite vem junto com o formulário).
 * Liberado mesmo com aceite pendente (AceitePendenteFilter). Exige login e token CSRF, como todo POST.
 */
@RestController
@RequestMapping("/api/legal")
public class LegalController {

    private final AceiteService aceiteService;
    private final TokenService tokenService;

    public LegalController(AceiteService aceiteService, TokenService tokenService) {
        this.aceiteService = aceiteService;
        this.tokenService = tokenService;
    }

    /** Grava o aceite e troca o cookie de login por um com o aceite dentro (é ele que libera o resto da API). */
    @PostMapping("/aceite")
    public UsuarioResponse aceitar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AceiteRequest dados,
                                   HttpServletResponse response) {
        AutenticacaoResultado resultado = aceiteService.aceitar(UsuarioLogado.de(jwt).id(), dados);
        // addHeader (e não ResponseEntity.header): mesmo motivo do AuthController.adicionarCookies
        response.addHeader(HttpHeaders.SET_COOKIE, tokenService.criarCookie(resultado.token()).toString());
        return resultado.usuario();
    }
}
