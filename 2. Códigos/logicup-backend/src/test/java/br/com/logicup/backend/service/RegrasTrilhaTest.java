package br.com.logicup.backend.service;

import br.com.logicup.backend.model.StatusRodada;
import br.com.logicup.backend.service.RegrasTrilha.ExercicioDaRodada;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
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

    // Sprint 7: rodada de 5 exercícios SEM Boss (ex.: Rodada 1 da Fase 1). 80% de 5 = 4.
    private static final List<ExercicioDaRodada> RODADA_SEM_BOSS = List.of(
            new ExercicioDaRodada(1, false), new ExercicioDaRodada(2, false), new ExercicioDaRodada(3, false),
            new ExercicioDaRodada(4, false), new ExercicioDaRodada(5, false));

    @Test
    void quatroDeCincoSemBossAprova() {
        // sem Boss na rodada, a exigência "Boss concluído" não bloqueia: vale só o mínimo de 80%
        assertTrue(RegrasTrilha.rodadaAprovada(RODADA_SEM_BOSS, Set.of(1, 2, 3, 5)));
    }

    @Test
    void tresDeCincoSemBossNaoAprova() {
        assertFalse(RegrasTrilha.rodadaAprovada(RODADA_SEM_BOSS, Set.of(1, 2, 3)));
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

    // ---------- Sprint 11: nivelamento ----------

    @Test
    void rodadasDoNivelamentoFicamLiberadasEDesbloqueiamASeguinte() {
        // 5 rodadas; o nivelamento liberou as 3 primeiras; nada aprovado ainda
        List<StatusRodada> status = RegrasTrilha.calcularStatus(
                List.of(false, false, false, false, false),
                List.of(true, true, true, false, false));
        // as liberadas pelo nivelamento NÃO viram APROVADA (o aluno não as fez); a 4ª libera; a 5ª espera
        assertEquals(List.of(LIBERADA, LIBERADA, LIBERADA, LIBERADA, BLOQUEADA), status);
    }

    @Test
    void rodadaNiveladaQueOAlunoFezContinuaAprovada() {
        List<StatusRodada> status = RegrasTrilha.calcularStatus(
                List.of(true, false, false), List.of(true, true, false));
        assertEquals(List.of(APROVADA, LIBERADA, LIBERADA), status);
    }

    @Test
    void semNivelamentoTudoContinuaComoAntes() {
        List<Boolean> aprovadas = List.of(true, false, false);
        assertEquals(RegrasTrilha.calcularStatus(aprovadas),
                RegrasTrilha.calcularStatus(aprovadas, List.of(false, false, false)));
        assertEquals(List.of(APROVADA, LIBERADA, BLOQUEADA), RegrasTrilha.calcularStatus(aprovadas));
    }

    @Test
    void listasDeTamanhosDiferentesSaoRecusadas() {
        assertThrows(IllegalArgumentException.class,
                () -> RegrasTrilha.calcularStatus(List.of(true), List.of(true, false)));
    }

    @Test
    void fasesDominadasContamEmSequenciaEParamNaPrimeiraFalha() {
        // acertou tudo nas Fases 1 e 2, errou uma na 3 e acertou tudo na 4 -> só 1 e 2 contam
        Map<Integer, List<Boolean>> acertos = Map.of(
                1, List.of(true, true), 2, List.of(true, true),
                3, List.of(true, false), 4, List.of(true, true), 5, List.of(false, false));
        assertEquals(2, RegrasTrilha.fasesDominadasEmSequencia(acertos));
    }

    @Test
    void errarAPrimeiraFaseNaoLiberaNada() {
        assertEquals(0, RegrasTrilha.fasesDominadasEmSequencia(
                Map.of(1, List.of(false, true), 2, List.of(true, true))));
    }

    @Test
    void acertarTudoLiberaTodasAsFasesDoTeste() {
        assertEquals(5, RegrasTrilha.fasesDominadasEmSequencia(Map.of(
                1, List.of(true, true), 2, List.of(true, true), 3, List.of(true, true),
                4, List.of(true, true), 5, List.of(true, true))));
    }

    @Test
    void sohFasesDeVerdadeSaoLiberadasPeloNivelamento() {
        assertTrue(RegrasTrilha.faseLiberadaPeloNivelamento(3, 3));
        assertFalse(RegrasTrilha.faseLiberadaPeloNivelamento(4, 3));
        assertFalse(RegrasTrilha.faseLiberadaPeloNivelamento(-300, 3)); // fases de teste (ordenação negativa)
        assertFalse(RegrasTrilha.faseLiberadaPeloNivelamento(1, 0));    // não fez o nivelamento
    }
}
