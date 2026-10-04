package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Substitui o que o DatabaseTestRunner verificava até a Sprint 5 (PE2):
 * o mapeamento de Turma (FK para o professor) e de AlunoTurma (chave composta + status).
 * Como todo @DataJpaTest, tudo é desfeito (rollback) no final.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TurmaAlunoTurmaRepositoryTest {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private AlunoTurmaRepository alunoTurmaRepository;

    @Test
    void deveMatricularAlunoNaTurmaDoProfessor() {
        Usuario professor = usuarioRepository.save(
                new Usuario("Professor Teste", "professor.teste.turma@teste.com", "hash", TipoUsuario.PROFESSOR));
        Turma turma = turmaRepository.save(new Turma("Turma de Teste", professor));
        Aluno aluno = alunoRepository.save(new Aluno("Aluno Teste", "aluno.teste.turma@teste.com", "hash"));

        alunoTurmaRepository.saveAndFlush(new AlunoTurma(aluno, turma));

        AlunoTurma matricula = alunoTurmaRepository
                .findById(new AlunoTurmaId(aluno.getId(), turma.getId()))
                .orElseThrow();

        assertEquals(StatusMatricula.ATIVO, matricula.getStatus());       // padrão
        assertEquals(aluno.getId(), matricula.getAluno().getId());
        assertEquals(professor.getId(), matricula.getTurma().getProfessor().getId());
    }
}
