package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Exercicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExercicioRepository extends JpaRepository<Exercicio, Integer> {

    // SELECT * FROM exercicio WHERE id_rodada = ? ORDER BY ordenacao ASC
    List<Exercicio> findByRodadaIdOrderByOrdenacaoAsc(Integer rodadaId);

    // SELECT * FROM exercicio WHERE codigo_questao = ?   (ex.: "F1.Q3")
    Optional<Exercicio> findByCodigoQuestao(String codigoQuestao);
}