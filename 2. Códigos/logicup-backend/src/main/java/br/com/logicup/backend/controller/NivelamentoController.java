package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.NivelamentoResponse;
import br.com.logicup.backend.dto.RespostasNivelamentoRequest;
import br.com.logicup.backend.dto.ResultadoNivelamentoResponse;
import br.com.logicup.backend.nivelamento.NivelamentoService;
import br.com.logicup.backend.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 11: quiz de nivelamento do aluno logado. Só ALUNO chega aqui (SecurityConfig).
 * O id do aluno vem SEMPRE do token, nunca do corpo: ninguém nivela a conta de outra pessoa.
 */
@RestController
@RequestMapping("/api/nivelamento")
public class NivelamentoController {

    private final NivelamentoService nivelamentoService;

    public NivelamentoController(NivelamentoService nivelamentoService) {
        this.nivelamentoService = nivelamentoService;
    }

    @GetMapping
    public NivelamentoResponse situacao(@AuthenticationPrincipal Jwt jwt) {
        return nivelamentoService.situacao(UsuarioLogado.de(jwt).id());
    }

    @PostMapping("/respostas")
    public ResultadoNivelamentoResponse responder(@AuthenticationPrincipal Jwt jwt,
                                                  @Valid @RequestBody RespostasNivelamentoRequest dados) {
        return nivelamentoService.responder(UsuarioLogado.de(jwt), dados);
    }

    /** "Prefiro começar do zero". 204 = sem corpo. */
    @PostMapping("/recusa")
    public ResponseEntity<Void> recusar(@AuthenticationPrincipal Jwt jwt) {
        nivelamentoService.recusar(UsuarioLogado.de(jwt).id());
        return ResponseEntity.noContent().build();
    }
}
