package br.com.logicup.backend.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Teste de unidade das regras do Aluno (sem Spring, sem banco). */
class AlunoTest {

    private static final LocalDate DIA_10 = LocalDate.of(2026, 10, 10);
    private Aluno aluno;

    @BeforeEach
    void setUp() {
        aluno = new Aluno("Aluno", "aluno", "a@a.com", "hash");
    }

    // ---------- pontos ----------

    @Test
    void novoAlunoComecaComValoresPadrao() {
        assertEquals(1, aluno.getNivelAtual());
        assertEquals(0, aluno.getPontuacaoAcumulada());
        assertEquals(0, aluno.getStreakAtual());
        assertNull(aluno.getUltimaAtividadeData());
    }

    @Test
    void adicionarPontosSomaNaPontuacao() {
        aluno.adicionarPontos(10);
        aluno.adicionarPontos(25);
        aluno.adicionarPontos(0); // resposta errada: 0 XP é permitido

        assertEquals(35, aluno.getPontuacaoAcumulada());
    }

    @Test
    void adicionarPontosNegativosEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> aluno.adicionarPontos(-5));
        assertEquals(0, aluno.getPontuacaoAcumulada()); // nada mudou
    }

    // ---------- streak ----------

    @Test
    void primeiraAtividadeComecaStreakEmUm() {
        aluno.registrarAtividade(DIA_10);

        assertEquals(1, aluno.getStreakAtual());
        assertEquals(DIA_10, aluno.getUltimaAtividadeData());
    }

    @Test
    void atividadeNoMesmoDiaNaoMudaStreak() {
        aluno.registrarAtividade(DIA_10);
        aluno.registrarAtividade(DIA_10);

        assertEquals(1, aluno.getStreakAtual());
    }

    @Test
    void atividadeEmDiasSeguidosAumentaStreak() {
        aluno.registrarAtividade(DIA_10);
        aluno.registrarAtividade(DIA_10.plusDays(1));
        aluno.registrarAtividade(DIA_10.plusDays(2));

        assertEquals(3, aluno.getStreakAtual());
    }

    @Test
    void pularUmDiaReiniciaStreak() {
        aluno.registrarAtividade(DIA_10);
        aluno.registrarAtividade(DIA_10.plusDays(1)); // streak 2
        aluno.registrarAtividade(DIA_10.plusDays(3)); // pulou o dia 12

        assertEquals(1, aluno.getStreakAtual());
        assertEquals(DIA_10.plusDays(3), aluno.getUltimaAtividadeData());
    }

    @Test
    void dataAnteriorAUltimaEhIgnorada() {
        aluno.registrarAtividade(DIA_10);
        aluno.registrarAtividade(DIA_10.plusDays(1)); // streak 2

        aluno.registrarAtividade(DIA_10.minusDays(5));

        assertEquals(2, aluno.getStreakAtual());
        assertEquals(DIA_10.plusDays(1), aluno.getUltimaAtividadeData());
    }

    @Test
    void dataNulaEhRecusada() {
        assertThrows(IllegalArgumentException.class, () -> aluno.registrarAtividade(null));
    }

    // ---------- streak exibido (vigente) ----------

    @Test
    void streakVigenteSeMantemAteOntem() {
        aluno.registrarAtividade(DIA_10);
        aluno.registrarAtividade(DIA_10.plusDays(1)); // streak 2, última atividade dia 11

        assertEquals(2, aluno.getStreakVigente(DIA_10.plusDays(1))); // hoje = 11
        assertEquals(2, aluno.getStreakVigente(DIA_10.plusDays(2))); // hoje = 12: ainda dá tempo
    }

    @Test
    void streakVigenteZeraDepoisDeUmDiaSemAtividade() {
        aluno.registrarAtividade(DIA_10);

        assertEquals(0, aluno.getStreakVigente(DIA_10.plusDays(2))); // pulou o dia 11
        assertEquals(1, aluno.getStreakAtual()); // o valor gravado só muda na próxima atividade
    }

    @Test
    void semAtividadeStreakVigenteEhZero() {
        assertEquals(0, aluno.getStreakVigente(DIA_10));
    }

    // ---------- nível ----------

    @Test
    void atualizarNivelSobeParaAFaseLiberada() {
        aluno.atualizarNivel(3);

        assertEquals(3, aluno.getNivelAtual());
    }

    @Test
    void nivelNuncaDiminui() {
        aluno.atualizarNivel(4);
        aluno.atualizarNivel(2);

        assertEquals(4, aluno.getNivelAtual());
    }

    @Test
    void nivelMenorQueUmEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> aluno.atualizarNivel(0));
        assertEquals(1, aluno.getNivelAtual());
    }
}
