package br.com.logicup.backend.dto;

import java.util.List;

/** Sprint 9 (R10): ranking da turma — todos os membros ativos, inclusive com 0 XP. Reaproveita a linha do ranking global. */
public record RankingTurmaResponse(List<PosicaoRankingResponse> posicoes) {
}
