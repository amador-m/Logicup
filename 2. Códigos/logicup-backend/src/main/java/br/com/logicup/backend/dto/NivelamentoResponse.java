package br.com.logicup.backend.dto;

import java.util.List;

/**
 * Sprint 11: GET /api/nivelamento.
 * questoes só vem preenchida quando a situação é PENDENTE; acertos/total/fasesLiberadas, quando é FEITO.
 */
public record NivelamentoResponse(SituacaoNivelamento situacao, List<QuestaoNivelamentoResponse> questoes,
                                  Integer acertos, Integer total, Integer fasesLiberadas) {
}
