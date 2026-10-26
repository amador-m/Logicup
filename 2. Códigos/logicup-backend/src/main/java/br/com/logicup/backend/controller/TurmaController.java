package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.RankingTurmaResponse;
import br.com.logicup.backend.dto.TarefaDaTurmaResponse;
import br.com.logicup.backend.dto.TarefaRequest;
import br.com.logicup.backend.dto.TurmaDetalheResponse;
import br.com.logicup.backend.dto.TurmaRequest;
import br.com.logicup.backend.dto.TurmaResumoResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.TurmaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Sprint 9: turmas do PROFESSOR. Só PROFESSOR chega aqui (SecurityConfig, R1).
 * O professor é sempre quem está no token (@AuthenticationPrincipal), nunca um id enviado na requisição.
 * O controller só traduz HTTP <-> serviço; as regras ficam no TurmaService.
 */
@RestController
@RequestMapping("/api/turmas")
public class TurmaController {

    private final TurmaService turmaService;

    public TurmaController(TurmaService turmaService) {
        this.turmaService = turmaService;
    }

    @PostMapping
    public ResponseEntity<TurmaResumoResponse> criar(@AuthenticationPrincipal Jwt jwt,
                                                     @Valid @RequestBody TurmaRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.criar(professor(jwt), dados));
    }

    @GetMapping
    public List<TurmaResumoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return turmaService.listar(professor(jwt));
    }

    @GetMapping("/{id}")
    public TurmaDetalheResponse detalhar(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        return turmaService.detalhar(professor(jwt), id);
    }

    @PutMapping("/{id}")
    public TurmaResumoResponse renomear(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                                        @Valid @RequestBody TurmaRequest dados) {
        return turmaService.renomear(professor(jwt), id, dados);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        turmaService.excluir(professor(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/codigo")
    public TurmaResumoResponse gerarNovoCodigo(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        return turmaService.gerarNovoCodigo(professor(jwt), id);
    }

    @DeleteMapping("/{id}/alunos/{alunoId}")
    public ResponseEntity<Void> removerAluno(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                                             @PathVariable Integer alunoId) {
        turmaService.removerAluno(professor(jwt), id, alunoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/ranking")
    public RankingTurmaResponse ranking(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id) {
        return turmaService.ranking(professor(jwt), id);
    }

    @PostMapping("/{id}/tarefas")
    public ResponseEntity<TarefaDaTurmaResponse> criarTarefa(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                                                             @Valid @RequestBody TarefaRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turmaService.criarTarefa(professor(jwt), id, dados));
    }

    @DeleteMapping("/{id}/tarefas/{tarefaId}")
    public ResponseEntity<Void> removerTarefa(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                                              @PathVariable Integer tarefaId) {
        turmaService.removerTarefa(professor(jwt), id, tarefaId);
        return ResponseEntity.noContent().build();
    }

    private static Integer professor(Jwt jwt) {
        return UsuarioLogado.de(jwt).id();
    }
}
