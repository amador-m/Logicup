package br.com.logicup.backend.dto;

import java.util.List;

/**
 * GET /api/ranking.
 *  topo: até 10 alunos com XP > 0, do maior para o menor;
 *  voce: a posição de quem está logado (null para professor/admin).
 * Só sai o USERNAME (nunca nome ou e-mail): minimização de dados (LGPD).
 */
public record RankingResponse(List<PosicaoRankingResponse> topo, PosicaoRankingResponse voce) {
}
