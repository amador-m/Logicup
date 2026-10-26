package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sprint 9: criar ou renomear turma (R2). Só o nome: o código é gerado pelo servidor (R3),
 * então um "codigo" enviado pelo navegador é simplesmente ignorado (o Jackson não tem onde colocá-lo).
 */
public record TurmaRequest(
        @NotBlank(message = "O nome da turma é obrigatório.")
        @Size(min = 3, max = 100, message = "O nome da turma deve ter entre 3 e 100 caracteres.")
        String nome
) {
    /** Tira os espaços das pontas ANTES da validação: "  ab  " vira "ab" e é recusado pelo @Size. */
    public TurmaRequest {
        nome = nome == null ? null : nome.strip();
    }
}
