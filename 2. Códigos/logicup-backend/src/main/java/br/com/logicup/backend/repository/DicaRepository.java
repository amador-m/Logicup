package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Dica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DicaRepository extends JpaRepository<Dica, Integer> {

    // dicas de UM exercício, na ordem de cadastro
    // SELECT * FROM dica WHERE id_exercicio = ? ORDER BY id_dica
    List<Dica> findByExercicioIdOrderByIdAsc(Integer exercicioId);

    // dicas de TODOS os exercícios de uma rodada, numa única consulta
    // (navega dica.exercicio.rodada.id -> o Spring Data gera um JOIN com exercicio)
    List<Dica> findByExercicioRodadaIdOrderByIdAsc(Integer rodadaId);
}