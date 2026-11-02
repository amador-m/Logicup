package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sprint 9 (ADR-001): botão "Executar" do laboratório. Um programa Java completo (a primeira classe tem o main).
 * Os limites de tamanho protegem o servidor: 10.000 caracteres equivalem a umas 300 linhas,
 * muito mais do que qualquer exercício do LogiCup precisa.
 */
public record ExecucaoRequest(
        @NotBlank(message = "Escreva um programa para executar.")
        @Size(max = 10_000, message = "O programa pode ter no máximo 10.000 caracteres.")
        String codigo,

        @Size(max = 2_000, message = "A entrada pode ter no máximo 2.000 caracteres.")
        String entrada
) {}
