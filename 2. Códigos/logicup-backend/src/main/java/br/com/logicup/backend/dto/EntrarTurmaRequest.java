package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

/** Sprint 9: o aluno entra na turma digitando o código de convite. */
public record EntrarTurmaRequest(
        @NotBlank(message = "Digite o código da turma.")
        String codigo
) {
    /** R4: normaliza o que a pessoa digitou — " ab3k 9mzq " vira "AB3K9MZQ". */
    public EntrarTurmaRequest {
        codigo = codigo == null ? null : codigo.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
    }
}
