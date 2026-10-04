package br.com.logicup.backend.service;

import br.com.logicup.backend.model.StatusRodada;
import br.com.logicup.backend.service.RegrasTrilha.ExercicioDaRodada;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static br.com.logicup.backend.model.StatusRodada.*;
import static org.junit.jupiter.api.Assertions.*;

class RegrasTrilhaTest {

    // rodada de 10 exercícios; o 10 é o Boss
    private static final List<ExercicioDaRodada> RODADA = List.of(
            new ExercicioDaRodada(1, false), new ExercicioDaRodada(2, false), new ExercicioDaRodada(3, false),
            new ExercicioDaRodada(4, false), new ExercicioDaRodada(5, false), new ExercicioDaRodada(6, false),
            new ExercicioDaRodada(7, false), new ExercicioDaRodada(8, false), new ExercicioDaRodada(9, false),
            new ExercicioDaRodada(10, true));

    @Test
    void oitoDeDezComBossAprova() {
        assertTrue(RegrasTrilha.rodadaAprovada(RODADA, Set.of(1, 2, 3, 4, 5, 6, 7, 10)));
    }

    @Test
    void seteDeDezComBossNaoAprova() {
        assertFalse(RegrasTrilha.rodadaAprovada(RODADA, Set.of(1, 2, 3, 4, 5, 6, 10)));
    }

    @Test
    void noveDeDezSemBossNaoAprova() {
        assertFalse(RegrasTrilha.rodadaAprovada(RODADA, Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    void rodadaVaziaNuncaAprova() {
        assertFalse(RegrasTrilha.rodadaAprovada(List.of(), Set.of()));
    }

    @Test
    void desbloqueioEmSequencia() {
        assertEquals(List.of(LIBERADA, BLOQUEADA, BLOQUEADA),
                RegrasTrilha.calcularStatus(List.of(false, false, false)));   // aluno novo
        assertEquals(List.of(APROVADA, LIBERADA, BLOQUEADA),
                RegrasTrilha.calcularStatus(List.of(true, false, false)));    // aprovou a 1ª
        assertEquals(List.of(APROVADA, APROVADA, LIBERADA),
                RegrasTrilha.calcularStatus(List.of(true, true, false)));
    }

    @Test
    void nivelEhAFaseMaisAltaLiberada() {
        List<Integer> fases = List.of(1, 1, 2, 3); // 4 rodadas: duas da fase 1, uma da 2, uma da 3
        List<StatusRodada> status = List.of(APROVADA, APROVADA, LIBERADA, BLOQUEADA);
        assertEquals(2, RegrasTrilha.faseMaisAltaLiberada(fases, status));
    }

    @Test
    void nivelNuncaFicaAbaixoDeUm() {
        assertEquals(1, RegrasTrilha.faseMaisAltaLiberada(List.of(-5), List.of(LIBERADA)));
    }
}
