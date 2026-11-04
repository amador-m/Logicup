package br.com.logicup.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sprint 10: "reenviar confirmação" e "esqueci minha senha" só precisam do e-mail. */
public record EmailRequest(
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email
) {
}
