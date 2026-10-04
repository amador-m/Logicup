package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O e-mail é obrigatório.") String email,
        @NotBlank(message = "A senha é obrigatória.") String senha
) {
    /** Mesmo cuidado do cadastro: espaços nas pontas do e-mail são removidos antes da validação. */
    public LoginRequest {
        email = email == null ? null : email.trim();
    }

    @Override
    public String toString() {
        return "LoginRequest[email=" + email + "]";
    }
}
