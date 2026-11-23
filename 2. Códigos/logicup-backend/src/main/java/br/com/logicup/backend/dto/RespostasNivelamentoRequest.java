package br.com.logicup.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Sprint 11: POST /api/nivelamento/respostas -> uma alternativa escolhida para cada questão. */
public record RespostasNivelamentoRequest(
        @NotEmpty(message = "Envie as respostas.")
        @Size(max = 50, message = "Respostas demais.")
        List<@Valid @NotNull Resposta> respostas
) {
    public record Resposta(
            @NotNull(message = "Questão não informada.") Integer questaoId,
            @NotBlank(message = "Escolha uma alternativa.") @Size(max = 5) String alternativa
    ) {
    }
}
