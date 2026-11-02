package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.ExecucaoExercicioRequest;
import br.com.logicup.backend.dto.ExecucaoRequest;
import br.com.logicup.backend.dto.ExecucaoResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.ExecucaoService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 9 (ADR-001): botão "Executar" (sem XP).
 *   POST /api/execucoes                  -> laboratório (programa completo)
 *   POST /api/exercicios/{id}/execucoes  -> dentro de uma questão de código (exemplos públicos)
 * Exige login: cai no anyRequest().authenticated() do SecurityConfig. Também exige o token CSRF,
 * como todo POST autenticado.
 */
@RestController
public class ExecucaoController {

    private final ExecucaoService execucaoService;

    public ExecucaoController(ExecucaoService execucaoService) {
        this.execucaoService = execucaoService;
    }

    @PostMapping("/api/execucoes")
    public ExecucaoResponse executar(@Valid @RequestBody ExecucaoRequest pedido, @AuthenticationPrincipal Jwt jwt) {
        return execucaoService.executar(pedido, UsuarioLogado.de(jwt));
    }

    @PostMapping("/api/exercicios/{id}/execucoes")
    public ExecucaoResponse executarExercicio(@PathVariable Integer id, @Valid @RequestBody ExecucaoExercicioRequest pedido,
                                              @AuthenticationPrincipal Jwt jwt) {
        return execucaoService.executarExercicio(id, pedido, UsuarioLogado.de(jwt));
    }
}
