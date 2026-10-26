package br.com.logicup.backend.dto;

import java.util.List;

/**
 * Sprint 9: GET /api/conquistas — o catálogo inteiro, com o progresso do aluno logado.
 * gemas = saldo atual (lido do banco).
 */
public record ConquistasResponse(int gemas, List<ConquistaResponse> conquistas) {
}
