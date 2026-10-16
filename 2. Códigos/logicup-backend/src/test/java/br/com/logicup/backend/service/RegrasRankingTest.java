package br.com.logicup.backend.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegrasRankingTest {

    @Test
    void semEmpateAsPosicoesSaoSequenciais() {
        assertEquals(List.of(1, 2, 3), RegrasRanking.posicoes(List.of(300, 200, 100)));
    }

    @Test
    void empateDividePosicaoEAProximaPula() {
        // 1º, 2º, 2º, 4º (ninguém fica em 3º)
        assertEquals(List.of(1, 2, 2, 4), RegrasRanking.posicoes(List.of(300, 200, 200, 100)));
    }

    @Test
    void todosEmpatadosFicamEmPrimeiro() {
        assertEquals(List.of(1, 1, 1), RegrasRanking.posicoes(List.of(50, 50, 50)));
    }

    @Test
    void listaVaziaNaoTemPosicoes() {
        assertEquals(List.of(), RegrasRanking.posicoes(List.of()));
    }

    @Test
    void posicaoEhUmMaisQuemTemMaisXp() {
        assertEquals(1, RegrasRanking.posicao(0));  // ninguém à frente
        assertEquals(4, RegrasRanking.posicao(3));
    }
}
