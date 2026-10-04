package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.AlunoTurma;
import br.com.logicup.backend.model.AlunoTurmaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlunoTurmaRepository extends JpaRepository<AlunoTurma, AlunoTurmaId> {
}