package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sprint 9 (ADR-001): botão "Executar" dentro de uma questão de código: só o código do aluno. */
public record ExecucaoExercicioRequest(
        @NotBlank(message = "Escreva o seu código antes de executar.")
        @Size(max = 10_000, message = "O código pode ter no máximo 10.000 caracteres.")
        String codigo
) {}
