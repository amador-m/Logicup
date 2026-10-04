package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.TrilhaResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.TrilhaService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trilha")
public class TrilhaController {

    private final TrilhaService trilhaService;

    public TrilhaController(TrilhaService trilhaService) {
        this.trilhaService = trilhaService;
    }

    /** Fases e rodadas com status e progresso + resumo do aluno (T4). */
    @GetMapping
    public TrilhaResponse trilha(@AuthenticationPrincipal Jwt jwt) {
        return trilhaService.montarTrilha(UsuarioLogado.de(jwt));
    }
}
