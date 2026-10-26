package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Turma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TurmaRepository extends JpaRepository<Turma, Integer> {

    /*
     * Sprint 9 (R8): a turma é SEMPRE buscada pelo id E pelo professor logado.
     * SELECT * FROM turma WHERE id_turma = ? AND id_professor = ?
     * Turma de outro professor e turma inexistente dão o mesmo resultado (vazio -> 404).
     */
    Optional<Turma> findByIdAndProfessorId(Integer id, Integer professorId);

    // turmas do professor, as mais novas primeiro
    List<Turma> findByProfessorIdOrderByIdDesc(Integer professorId);

    // R4: o aluno entra pelo código (UNIQUE no banco)
    Optional<Turma> findByCodigoConvite(String codigoConvite);

    boolean existsByCodigoConvite(String codigoConvite);
}
