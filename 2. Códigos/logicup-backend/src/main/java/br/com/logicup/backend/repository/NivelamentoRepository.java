package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Nivelamento;
import org.springframework.data.jpa.repository.JpaRepository;

/** Sprint 11: o id do Nivelamento é o id do aluno (findById(alunoId) traz o nivelamento dele). */
public interface NivelamentoRepository extends JpaRepository<Nivelamento, Integer> {
}
