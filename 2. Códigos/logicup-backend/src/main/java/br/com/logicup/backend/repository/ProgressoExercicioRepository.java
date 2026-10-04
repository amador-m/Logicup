package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.ProgressoExercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressoExercicioRepository extends JpaRepository<ProgressoExercicio, Integer> {

    // a linha de progresso de UM aluno em UM exercício (existe no máximo uma — UNIQUE)
    Optional<ProgressoExercicio> findByAlunoIdAndExercicioId(Integer alunoId, Integer exercicioId);

    // progresso do aluno em todos os exercícios de UMA rodada (correção e "rodada aprovada?" - PR5)
    List<ProgressoExercicio> findByAlunoIdAndExercicioRodadaId(Integer alunoId, Integer rodadaId);

    // todo o progresso do aluno (no máximo ~110 linhas) -> calcular o desbloqueio da trilha inteira
    List<ProgressoExercicio> findByAlunoId(Integer alunoId);
}