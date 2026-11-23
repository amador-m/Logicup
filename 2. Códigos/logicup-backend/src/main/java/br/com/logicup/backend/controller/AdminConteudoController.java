package br.com.logicup.backend.controller;

import br.com.logicup.backend.admin.AdminConteudoService;
import br.com.logicup.backend.dto.ConteudoAdminResponse;
import br.com.logicup.backend.dto.EditarExercicioRequest;
import br.com.logicup.backend.dto.EditarRodadaRequest;
import br.com.logicup.backend.dto.RodadaAdminResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Sprint 11: gestão de conteúdos (só ADMIN: SecurityConfig, /api/admin/**). */
@RestController
@RequestMapping("/api/admin")
public class AdminConteudoController {

    private final AdminConteudoService adminConteudoService;

    public AdminConteudoController(AdminConteudoService adminConteudoService) {
        this.adminConteudoService = adminConteudoService;
    }

    @GetMapping("/conteudo")
    public ConteudoAdminResponse arvore() {
        return adminConteudoService.arvore();
    }

    @GetMapping("/rodadas/{id}")
    public RodadaAdminResponse rodada(@PathVariable Integer id) {
        return adminConteudoService.rodada(id);
    }

    @PutMapping("/rodadas/{id}")
    public RodadaAdminResponse editarRodada(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                                            @Valid @RequestBody EditarRodadaRequest dados) {
        return adminConteudoService.editarRodada(UsuarioLogado.de(jwt).id(), id, dados);
    }

    @PutMapping("/exercicios/{id}")
    public RodadaAdminResponse.Exercicio editarExercicio(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                                                         @Valid @RequestBody EditarExercicioRequest dados) {
        return adminConteudoService.editarExercicio(UsuarioLogado.de(jwt).id(), id, dados);
    }
}
