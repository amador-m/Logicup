package br.com.logicup.backend.dto;

import java.util.List;

/** POST /api/rodadas/{id}/respostas: correção de cada exercício + resumo do envio. */
public record CorrecaoResponse(List<ResultadoExercicioResponse> resultados, ResumoEnvioResponse resumo) {
}
