package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.AlunoTurma;
import br.com.logicup.backend.model.AlunoTurmaId;
import br.com.logicup.backend.model.StatusMatricula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlunoTurmaRepository extends JpaRepository<AlunoTurma, AlunoTurmaId> {

    // Sprint 9 (R5): a matrícula ATIVA do aluno, se houver (no máximo uma)
    Optional<AlunoTurma> findFirstByAlunoIdAndStatus(Integer alunoId, StatusMatricula status);

    // Sprint 9 (R9, R10): membros com um status (ex.: ATIVO), para acompanhamento e ranking
    List<AlunoTurma> findByTurmaIdAndStatus(Integer turmaId, StatusMatricula status);

    long countByTurmaIdAndStatus(Integer turmaId, StatusMatricula status);

    // Sprint 9 (R14): todas as matrículas da turma, em qualquer status (para apagar junto com ela)
    List<AlunoTurma> findByTurmaId(Integer turmaId);

    // Sprint 9 (conquista "Colega de turma"): o aluno já entrou em alguma turma, em qualquer status?
    boolean existsByAlunoId(Integer alunoId);
}
