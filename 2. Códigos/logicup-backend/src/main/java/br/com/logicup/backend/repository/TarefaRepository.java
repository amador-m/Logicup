package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Tarefa;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Sprint 9: tarefas = rodadas atribuídas a uma turma (R11). */
@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Integer> {

    // traz a rodada e a fase JUNTO (a tela mostra os títulos), sem uma consulta extra por tarefa (N+1)
    @EntityGraph(attributePaths = {"rodada", "rodada.modulo"})
    List<Tarefa> findByTurmaIdOrderByIdAsc(Integer turmaId);

    // R11: a mesma rodada não entra duas vezes na mesma turma (também há UNIQUE no banco)
    boolean existsByTurmaIdAndRodadaId(Integer turmaId, Integer rodadaId);

    // R8: a tarefa só é encontrada DENTRO da turma informada
    Optional<Tarefa> findByIdAndTurmaId(Integer id, Integer turmaId);

}
