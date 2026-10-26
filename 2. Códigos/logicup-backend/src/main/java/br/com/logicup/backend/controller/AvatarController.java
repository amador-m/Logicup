package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.AvataresResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.AvatarService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 9: catálogo e loja de avatares.
 * GET: qualquer usuário logado. POST .../compra: só ALUNO (SecurityConfig), porque só aluno tem gemas.
 * Trocar o avatar EM USO fica no PerfilController (PUT /api/perfil/avatar): é uma edição do perfil.
 */
@RestController
@RequestMapping("/api/avatares")
public class AvatarController {

    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @GetMapping
    public AvataresResponse catalogo(@AuthenticationPrincipal Jwt jwt) {
        return avatarService.catalogo(UsuarioLogado.de(jwt));
    }

    /** Devolve o catálogo atualizado (saldo novo e o avatar já marcado como "possui"). */
    @PostMapping("/{codigo}/compra")
    public AvataresResponse comprar(@AuthenticationPrincipal Jwt jwt, @PathVariable String codigo) {
        return avatarService.comprar(UsuarioLogado.de(jwt), codigo);
    }
}
