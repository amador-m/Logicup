package br.com.logicup.backend.dto;

import java.util.List;

/**
 * Sprint 9: uma conquista do catálogo, com o valor atual do aluno (ex.: 7 dias de ofensiva)
 * e todos os níveis (desbloqueados ou não), para a tela mostrar o que falta para o próximo.
 */
public record ConquistaResponse(String codigo, String titulo, int valorAtual, List<NivelConquistaResponse> niveis) {
}
