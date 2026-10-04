package br.com.logicup.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Teste de UNIDADE: testa só a regra de negócio da classe, sem Spring e sem banco.
 * Por isso é muito rápido (milissegundos) — é a vantagem prática da opção R1-B.
 */
class ProgressoExercicioTest {

    private static final String RESPOSTA = "{\"alternativa\": \"a\"}";
    private ProgressoExercicio progresso;

    @BeforeEach
    void setUp() {
        Exercicio exercicio = new Exercicio(null, "TESTE.Q1", "Questão", 1,
                TipoExercicio.VERDADEIRO_FALSO, FormatoResposta.ESCOLHA_UNICA, 25,
                "Enunciado", "{\"correta\": \"a\"}", "Explicação");
        progresso = new ProgressoExercicio(new Aluno("Aluno", "a@a.com", "hash"), exercicio);
    }

    @Test
    void novoProgressoComecaZerado() {
        assertEquals(StatusProgresso.EM_ANDAMENTO, progresso.getStatus());
        assertEquals(0, progresso.getTentativas());
        assertEquals(0, progresso.getPontosGanhos());
    }

    @Test
    void errarContaTentativaMasNaoDaXp() {
        int xp = progresso.registrarResposta(RESPOSTA, false);

        assertEquals(0, xp);
        assertEquals(1, progresso.getTentativas());                   // PR3
        assertEquals(StatusProgresso.EM_ANDAMENTO, progresso.getStatus());
        assertEquals(RESPOSTA, progresso.getRespostaJson());          // PR4
    }

    @Test
    void acertarPelaPrimeiraVezConcluiEDaXp() {
        int xp = progresso.registrarResposta(RESPOSTA, true);

        assertEquals(25, xp);                                          // PR2
        assertEquals(25, progresso.getPontosGanhos());
        assertTrue(progresso.isConcluido());                           // PR1
    }

    @Test
    void acertarDeNovoNaoDaXpRepetido() {
        progresso.registrarResposta(RESPOSTA, true);
        int xpSegundaVez = progresso.registrarResposta(RESPOSTA, true);

        assertEquals(0, xpSegundaVez);                                 // PR2: sem "farm" de XP
        assertEquals(25, progresso.getPontosGanhos());                 // continua 25, não 50
        assertEquals(2, progresso.getTentativas());                    // PR3: mas conta a tentativa
    }

    @Test
    void errarDepoisDeConcluidoNaoVoltaStatus() {
        progresso.registrarResposta(RESPOSTA, true);
        progresso.registrarResposta("{\"alternativa\": \"b\"}", false);

        assertTrue(progresso.isConcluido());                           // PR1: nunca volta
        assertEquals(25, progresso.getPontosGanhos());
        assertEquals(2, progresso.getTentativas());                    // PR3: as duas respostas contam
    }

    // ---------- PR7: XP reduzido quando acerta depois de errar ----------

    @Test
    void acertarDepoisDeErrarDaMetadeDoXp() {
        progresso.registrarResposta("{\"alternativa\": \"b\"}", false);
        int xp = progresso.registrarResposta(RESPOSTA, true);

        assertEquals(12, xp);                                          // 25 / 2 = 12 (arredonda para baixo)
        assertEquals(12, progresso.getPontosGanhos());
        assertTrue(progresso.isConcluido());
    }

    @Test
    void reducaoEhFixaMesmoComVariosErros() {
        progresso.registrarResposta("{\"alternativa\": \"b\"}", false);
        progresso.registrarResposta("{\"alternativa\": \"c\"}", false);
        progresso.registrarResposta("{\"alternativa\": \"d\"}", false);
        int xp = progresso.registrarResposta(RESPOSTA, true);

        assertEquals(12, xp);                                          // continua metade, não cai mais
        assertEquals(4, progresso.getTentativas());
    }

    @Test
    void depoisDoAcertoReduzidoNaoGanhaMaisNada() {
        progresso.registrarResposta("{\"alternativa\": \"b\"}", false);
        progresso.registrarResposta(RESPOSTA, true);
        int xp = progresso.registrarResposta(RESPOSTA, true);

        assertEquals(0, xp);                                           // PR2 continua valendo
        assertEquals(12, progresso.getPontosGanhos());                 // não "completa" os 25
    }

    @Test
    void respostaNulaNaoEhAceita() {
        assertThrows(NullPointerException.class, () -> progresso.registrarResposta(null, true));
        assertEquals(0, progresso.getTentativas()); // nada mudou
    }
}
