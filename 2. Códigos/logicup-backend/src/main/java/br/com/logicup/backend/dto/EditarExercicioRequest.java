package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sprint 11: PUT /api/admin/exercicios/{id} -> só os TEXTOS (gabarito, alternativas e tipo não mudam aqui). */
public record EditarExercicioRequest(
        @NotBlank(message = "O título não pode ficar vazio.")
        @Size(max = 150, message = "O título pode ter no máximo 150 caracteres.")
        String titulo,
        @NotBlank(message = "O enunciado não pode ficar vazio.")
        @Size(max = 20_000, message = "O enunciado ficou grande demais.")
        String enunciado,
        @NotBlank(message = "A explicação não pode ficar vazia.")
        @Size(max = 20_000, message = "A explicação ficou grande demais.")
        String explicacao
) {
}
