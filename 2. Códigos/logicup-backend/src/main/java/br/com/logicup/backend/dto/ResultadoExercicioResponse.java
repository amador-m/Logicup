package br.com.logicup.backend.dto;

/**
 * Correção de UM exercício (T1: feedback completo).
 * respostaCorreta: formato próprio para exibição (alternativa, lista, pares, texto) — nunca o gabarito cru.
 */
public record ResultadoExercicioResponse(Integer exercicioId, boolean correta, int xpGanho,
                                         String explicacao, Object respostaCorreta) {
}
