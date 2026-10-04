package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ExercicioRepositoryTest {

    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;

    private Rodada criarRodada() {
        Modulo modulo = moduloRepository.save(new Modulo("Módulo de teste", null, 997));
        return rodadaRepository.save(new Rodada(modulo, "Rodada de teste", 1, "{\"teoria\": [], \"exemplos\": []}"));
    }

    private Exercicio novoExercicio(Rodada rodada, String codigo, int ordem) {
        Exercicio e = new Exercicio(rodada, codigo, "Questão " + ordem, ordem,
                TipoExercicio.VERDADEIRO_FALSO, FormatoResposta.ESCOLHA_UNICA, 10,
                "Enunciado de teste", "{\"correta\": \"a\"}", "Explicação de teste");
        e.setAlternativasJson("[{\"id\": \"a\", \"texto\": \"Verdadeiro\"}, {\"id\": \"b\", \"texto\": \"Falso\"}]");
        return e;
    }

    @Test
    void deveSalvarEBuscarExerciciosDaRodadaEmOrdem() {
        Rodada rodada = criarRodada();
        exercicioRepository.save(novoExercicio(rodada, "TESTE.Q2", 2));
        exercicioRepository.save(novoExercicio(rodada, "TESTE.Q1", 1));

        List<Exercicio> exercicios = exercicioRepository.findByRodadaIdOrderByOrdenacaoAsc(rodada.getId());

        assertEquals(2, exercicios.size());
        assertEquals("TESTE.Q1", exercicios.get(0).getCodigoQuestao());
        assertEquals(FormatoResposta.ESCOLHA_UNICA, exercicios.get(0).getFormatoResposta()); // enum lido do banco
        assertNull(exercicios.get(0).getDificuldade()); // dificuldade opcional (S3)
    }

    @Test
    void naoDevePermitirCodigoDeQuestaoRepetido() {
        Rodada rodada = criarRodada();
        exercicioRepository.save(novoExercicio(rodada, "TESTE.Q1", 1));

        // saveAndFlush força o INSERT na hora, então o banco já barra o UNIQUE aqui
        assertThrows(DataIntegrityViolationException.class,
                () -> exercicioRepository.saveAndFlush(novoExercicio(rodada, "TESTE.Q1", 2)));
    }
}