package br.com.logicup.backend.dto;

import java.util.List;

/**
 * Sprint 11: resultado do teste de nivelamento. Depois de responder, o aluno vê a correção de cada questão
 * (certa ou errada, qual era a resposta e por quê): o nivelamento também ensina.
 *
 * @param fasesLiberadas fases dominadas em sequência desde a Fase 1 (0 = começa do início)
 * @param nivelAtual     o nível do aluno depois do nivelamento (= fase mais alta liberada)
 */
public record ResultadoNivelamentoResponse(int acertos, int total, int fasesLiberadas, int nivelAtual,
                                           List<Correcao> correcoes) {

    public record Correcao(Integer questaoId, Integer fase, boolean acertou, String correta, String explicacao) {
    }
}
