package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProgressoExercicioRepositoryTest {

    @Autowired private AlunoRepository alunoRepository;
    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private ProgressoExercicioRepository progressoRepository;

    private Aluno aluno;
    private Rodada rodada;
    private Exercicio q1;
    private Exercicio q2;

    @BeforeEach // roda antes de CADA teste (e tudo é desfeito no rollback)
    void prepararDados() {
        aluno = alunoRepository.save(new Aluno("Aluno Teste", "aluno.teste.progresso@teste.com", "hash"));
        Modulo modulo = moduloRepository.save(new Modulo("Módulo de teste", null, 995));
        rodada = rodadaRepository.save(new Rodada(modulo, "Rodada de teste", 1, "{\"teoria\": [], \"exemplos\": []}"));
        q1 = criarExercicio("TESTE.Q1", 1);
        q2 = criarExercicio("TESTE.Q2", 2);
    }

    private Exercicio criarExercicio(String codigo, int ordem) {
        return exercicioRepository.save(new Exercicio(rodada, codigo, "Questão " + ordem, ordem,
                TipoExercicio.VERDADEIRO_FALSO, FormatoResposta.ESCOLHA_UNICA, 10,
                "Enunciado de teste", "{\"correta\": \"a\"}", "Explicação de teste"));
    }

    @Test
    void deveSalvarComValoresPadraoEBuscarPorAlunoEExercicio() {
        ProgressoExercicio p = new ProgressoExercicio(aluno, q1);
        p.registrarResposta("{\"alternativa\": \"b\"}", false); // errou (gabarito é "a")
        progressoRepository.save(p);

        ProgressoExercicio encontrado = progressoRepository
                .findByAlunoIdAndExercicioId(aluno.getId(), q1.getId())
                .orElseThrow();

        assertEquals(StatusProgresso.EM_ANDAMENTO, encontrado.getStatus()); // padrão
        assertEquals(0, encontrado.getPontosGanhos());                      // padrão
        assertEquals(1, encontrado.getTentativas());
    }

    @Test
    void deveBuscarProgressoDoAlunoNaRodada() {
        progressoRepository.save(new ProgressoExercicio(aluno, q1));
        progressoRepository.save(new ProgressoExercicio(aluno, q2));

        List<ProgressoExercicio> daRodada = progressoRepository
                .findByAlunoIdAndExercicioRodadaId(aluno.getId(), rodada.getId());

        assertEquals(2, daRodada.size());
    }

    @Test
    void naoDevePermitirDuasLinhasParaOMesmoAlunoEExercicio() {
        progressoRepository.save(new ProgressoExercicio(aluno, q1));

        assertThrows(DataIntegrityViolationException.class,
                () -> progressoRepository.saveAndFlush(new ProgressoExercicio(aluno, q1)));
    }
}