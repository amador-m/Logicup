package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DicaRepositoryTest {

    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private DicaRepository dicaRepository;

    private Exercicio criarExercicio(Rodada rodada, String codigo, int ordem) {
        return exercicioRepository.save(new Exercicio(rodada, codigo, "Questão " + ordem, ordem,
                TipoExercicio.PREVER_SAIDA, FormatoResposta.RESPOSTA_CURTA, 25,
                "Enunciado de teste", "{\"aceitas\": [\"27\"]}", "Explicação de teste"));
    }

    @Test
    void deveBuscarDicasDoExercicioEDaRodada() {
        Modulo modulo = moduloRepository.save(new Modulo("Módulo de teste", null, 996));
        Rodada rodada = rodadaRepository.save(new Rodada(modulo, "Rodada de teste", 1, "{\"teoria\": [], \"exemplos\": []}"));
        Exercicio q1 = criarExercicio(rodada, "TESTE.Q1", 1);
        Exercicio q2 = criarExercicio(rodada, "TESTE.Q2", 2);

        dicaRepository.save(new Dica(q1, "Primeira dica da Q1"));
        dicaRepository.save(new Dica(q1, "Segunda dica da Q1"));
        dicaRepository.save(new Dica(q2, "Dica da Q2"));

        List<Dica> dicasQ1 = dicaRepository.findByExercicioIdOrderByIdAsc(q1.getId());
        assertEquals(2, dicasQ1.size());
        assertEquals("Primeira dica da Q1", dicasQ1.get(0).getTextoDica()); // ordem de cadastro

        List<Dica> dicasDaRodada = dicaRepository.findByExercicioRodadaIdOrderByIdAsc(rodada.getId());
        assertEquals(3, dicasDaRodada.size()); // Q1 + Q2 numa consulta só
    }
}