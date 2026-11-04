package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sprint 10: o código que estava no link do e-mail (depois do "#"). Vai no CORPO de um POST, e não na URL,
 * para não ficar gravado em histórico do navegador, logs de servidor ou cabeçalho Referer.
 */
public record ConfirmarEmailRequest(
        @NotBlank(message = "Link inválido.")
        @Size(max = 100, message = "Link inválido.")
        String token
) {
    @Override
    public String toString() { // o código é secreto
        return "ConfirmarEmailRequest[***]";
    }
}
