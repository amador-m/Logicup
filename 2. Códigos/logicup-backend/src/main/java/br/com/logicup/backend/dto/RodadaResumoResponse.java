package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.StatusRodada;

/** Uma rodada no mapa da trilha, com o progresso do aluno nela. */
public record RodadaResumoResponse(Integer id, Integer ordem, String titulo, StatusRodada status,
                                   int exerciciosConcluidos, int totalExercicios, int xpGanho, int xpTotal) {
}
