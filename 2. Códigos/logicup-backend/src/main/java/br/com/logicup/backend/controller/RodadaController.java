package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.CorrecaoResponse;
import br.com.logicup.backend.dto.EnvioRespostasRequest;
import br.com.logicup.backend.dto.RodadaDetalheResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.RespostaService;
import br.com.logicup.backend.service.RodadaService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rodadas")
public class RodadaController {

    private final RodadaService rodadaService;
    private final RespostaService respostaService;

    public RodadaController(RodadaService rodadaService, RespostaService respostaService) {
        this.rodadaService = rodadaService;
        this.respostaService = respostaService;
    }

    /** Conteúdo e exercícios da rodada (sem gabarito). */
    @GetMapping("/{id}")
    public RodadaDetalheResponse detalhar(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        return rodadaService.detalhar(id, UsuarioLogado.de(jwt));
    }

    /** D4: respostas da rodada numa única requisição. Só ALUNO (regra no SecurityConfig). */
    @PostMapping("/{id}/respostas")
    public CorrecaoResponse responder(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt,
                                      @Valid @RequestBody EnvioRespostasRequest envio) {
        return respostaService.enviar(id, UsuarioLogado.de(jwt), envio);
    }
}
