package br.com.logicup.backend.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Regras PURAS do ranking (sem banco, sem Spring) — testadas no RegrasRankingTest.
 *
 * Empate (decisão da Sprint 7, opção A): quem tem o MESMO XP fica na MESMA posição,
 * e a posição seguinte "pula" (1º, 2º, 2º, 4º). É o padrão de competições ("standard competition ranking"):
 * a posição de alguém = 1 + quantas pessoas têm MAIS XP que ele.
 */
public final class RegrasRanking {

    private RegrasRanking() {}

    /**
     * @param xpsEmOrdemDecrescente XP de cada aluno, do maior para o menor (ex.: 300, 200, 200, 100)
     * @return a posição de cada um, na mesma ordem (ex.: 1, 2, 2, 4)
     */
    public static List<Integer> posicoes(List<Integer> xpsEmOrdemDecrescente) {
        List<Integer> posicoes = new ArrayList<>();
        for (int i = 0; i < xpsEmOrdemDecrescente.size(); i++) {
            boolean empatouComOAnterior = i > 0 && xpsEmOrdemDecrescente.get(i).equals(xpsEmOrdemDecrescente.get(i - 1));
            posicoes.add(empatouComOAnterior ? posicoes.get(i - 1) : i + 1);
        }
        return posicoes;
    }

    /** Posição de um aluno a partir de quantos alunos têm MAIS XP que ele. */
    public static int posicao(long alunosComMaisXp) {
        return (int) alunosComMaisXp + 1;
    }
}
