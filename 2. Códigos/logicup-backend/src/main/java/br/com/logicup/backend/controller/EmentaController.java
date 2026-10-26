package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.EmentaResponse;
import br.com.logicup.backend.service.EmentaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 7: ementa do professor. Quem pode acessar é decidido no SecurityConfig
 * (PROFESSOR e ADMIN -> 200; ALUNO -> 403; sem login -> 401), antes de chegar aqui.
 */
@RestController
@RequestMapping("/api/ementa")
public class EmentaController {

    private final EmentaService ementaService;

    public EmentaController(EmentaService ementaService) {
        this.ementaService = ementaService;
    }

    /** Fases -> rodadas (objetivos) -> questões (habilidade). Sem enunciado, alternativas nem gabarito. */
    @GetMapping
    public EmentaResponse ementa() {
        return ementaService.montar();
    }
}
