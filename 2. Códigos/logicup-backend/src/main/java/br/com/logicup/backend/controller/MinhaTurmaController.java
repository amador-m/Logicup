package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.EntrarTurmaRequest;
import br.com.logicup.backend.dto.MinhaTurmaResponse;
import br.com.logicup.backend.dto.RankingTurmaResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.MinhaTurmaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 9: "Minha turma", o lado do ALUNO. Só ALUNO chega aqui (SecurityConfig, R1).
 * Não há id de turma na URL: a turma é a matrícula ATIVA de quem está no token.
 */
@RestController
@RequestMapping("/api/minha-turma")
public class MinhaTurmaController {

    private final MinhaTurmaService minhaTurmaService;

    public MinhaTurmaController(MinhaTurmaService minhaTurmaService) {
        this.minhaTurmaService = minhaTurmaService;
    }

    /** 204 = sem turma (o frontend mostra o campo do código). Não é erro, por isso não é 404. */
    @GetMapping
    public ResponseEntity<MinhaTurmaResponse> buscar(@AuthenticationPrincipal Jwt jwt) {
        return minhaTurmaService.buscar(aluno(jwt))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    public MinhaTurmaResponse entrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody EntrarTurmaRequest dados) {
        return minhaTurmaService.entrar(aluno(jwt), dados);
    }

    @GetMapping("/ranking")
    public RankingTurmaResponse ranking(@AuthenticationPrincipal Jwt jwt) {
        return minhaTurmaService.ranking(aluno(jwt));
    }

    @DeleteMapping
    public ResponseEntity<Void> sair(@AuthenticationPrincipal Jwt jwt) {
        minhaTurmaService.sair(aluno(jwt));
        return ResponseEntity.noContent().build();
    }

    private static Integer aluno(Jwt jwt) {
        return UsuarioLogado.de(jwt).id();
    }
}
