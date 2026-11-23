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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sprint 9: o lado do ALUNO nas turmas. Só ALUNO chega aqui (SecurityConfig, R1).
 * Sprint 11: "minha-turma" virou "minhas-turmas", porque o aluno pode estar em várias. O id da turma na URL só
 * escolhe entre as turmas em que o aluno do TOKEN tem matrícula ativa (o service confere).
 */
@RestController
@RequestMapping("/api/minhas-turmas")
public class MinhaTurmaController {

    private final MinhaTurmaService minhaTurmaService;

    public MinhaTurmaController(MinhaTurmaService minhaTurmaService) {
        this.minhaTurmaService = minhaTurmaService;
    }

    /** Lista vazia = nenhuma turma (a tela mostra o campo do código). */
    @GetMapping
    public List<MinhaTurmaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return minhaTurmaService.listar(aluno(jwt));
    }

    @PostMapping
    public MinhaTurmaResponse entrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody EntrarTurmaRequest dados) {
        return minhaTurmaService.entrar(aluno(jwt), dados);
    }

    @GetMapping("/{turmaId}/ranking")
    public RankingTurmaResponse ranking(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer turmaId) {
        return minhaTurmaService.ranking(aluno(jwt), turmaId);
    }

    @DeleteMapping("/{turmaId}")
    public ResponseEntity<Void> sair(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer turmaId) {
        minhaTurmaService.sair(aluno(jwt), turmaId);
        return ResponseEntity.noContent().build();
    }

    private static Integer aluno(Jwt jwt) {
        return UsuarioLogado.de(jwt).id();
    }
}
