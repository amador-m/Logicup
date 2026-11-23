package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

/**
 * Sprint 11: PUT /api/admin/rodadas/{id}. "conteudo" é o JSON da teoria com os textos editados
 * (a forma precisa ser a mesma do atual: ver RegrasEdicaoConteudo).
 */
public record EditarRodadaRequest(
        @NotBlank(message = "O título não pode ficar vazio.")
        @Size(max = 150, message = "O título pode ter no máximo 150 caracteres.")
        String titulo,
        @NotNull(message = "Envie o conteúdo da rodada.")
        JsonNode conteudo
) {
}
