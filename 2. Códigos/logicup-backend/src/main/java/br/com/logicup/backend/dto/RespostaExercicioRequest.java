package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Map;

/** Resposta a UM exercício. "resposta" segue o formato do exercício, ex.: {"alternativa":"a"}. */
public record RespostaExercicioRequest(
        @NotNull(message = "Informe o exercício.") Integer exercicioId,
        @NotNull(message = "Informe a resposta.") Map<String, Object> resposta
) {
}
