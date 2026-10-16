package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Sprint 7: o aluno entra com o E-MAIL ou com o NOME DE USUÁRIO, no mesmo campo "login".
 * Como o username não aceita "@", basta ver se o texto tem "@" para saber qual dos dois é (AuthService).
 */
public record LoginRequest(
        @NotBlank(message = "Informe seu e-mail ou nome de usuário.") String login,
        @NotBlank(message = "A senha é obrigatória.") String senha
) {
    /** Mesmo cuidado do cadastro: espaços nas pontas são removidos antes da validação. */
    public LoginRequest {
        login = login == null ? null : login.trim();
    }

    public boolean ehEmail() {
        return login != null && login.contains("@");
    }

    @Override
    public String toString() {
        return "LoginRequest[login=" + login + "]";
    }
}
