package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Sprint 10: o código do link de "esqueci minha senha" + a senha nova (mesma regra do cadastro). */
public record RedefinirSenhaRequest(
        @NotBlank(message = "Link inválido.")
        @Size(max = 100, message = "Link inválido.")
        String token,

        @NotBlank(message = "Informe a nova senha.")
        @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres.")
        String novaSenha
) {
    @Override
    public String toString() { // nem o código nem a senha vão para log
        return "RedefinirSenhaRequest[***]";
    }
}
