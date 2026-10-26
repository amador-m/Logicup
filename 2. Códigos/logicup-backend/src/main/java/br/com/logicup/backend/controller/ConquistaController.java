package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.ConquistasResponse;
import br.com.logicup.backend.dto.NovaConquistaResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.ConquistaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sprint 9: conquistas do aluno logado. Só ALUNO chega aqui (SecurityConfig).
 * Os dois GET só LEEM (decisão B1): quem grava medalhas é o envio de rodada, a entrada em turma e o login.
 */
@RestController
@RequestMapping("/api/conquistas")
public class ConquistaController {

    private final ConquistaService conquistaService;

    public ConquistaController(ConquistaService conquistaService) {
        this.conquistaService = conquistaService;
    }

    @GetMapping
    public ConquistasResponse listar(@AuthenticationPrincipal Jwt jwt) {
        return conquistaService.listar(aluno(jwt));
    }

    /** As ainda não vistas (lista vazia = nada para comemorar). */
    @GetMapping("/novas")
    public List<NovaConquistaResponse> novas(@AuthenticationPrincipal Jwt jwt) {
        return conquistaService.novas(aluno(jwt));
    }

    /** A comemoração foi mostrada: marca todas como vistas. 204 = sem corpo. */
    @PostMapping("/vistas")
    public ResponseEntity<Void> marcarVistas(@AuthenticationPrincipal Jwt jwt) {
        conquistaService.marcarVistas(aluno(jwt));
        return ResponseEntity.noContent().build();
    }

    private static Integer aluno(Jwt jwt) {
        return UsuarioLogado.de(jwt).id();
    }
}
