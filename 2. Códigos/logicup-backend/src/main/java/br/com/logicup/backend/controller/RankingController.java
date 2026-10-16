package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.RankingResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.RankingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exige login (regra "anyRequest().authenticated()" do SecurityConfig): o ranking não é público. */
@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    /** Top 10 por XP + a posição de quem está logado (o id vem do TOKEN, nunca de parâmetro). */
    @GetMapping
    public RankingResponse ranking(@AuthenticationPrincipal Jwt jwt) {
        return rankingService.montar(UsuarioLogado.de(jwt));
    }
}
