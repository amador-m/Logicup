package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sprint 7: troca de senha pelo próprio usuário.
 * A senha ATUAL é obrigatória: se alguém encontrar o computador com a sessão aberta,
 * não consegue trocar a senha (e "roubar" a conta) sem saber a senha antiga.
 */
public record AlterarSenhaRequest(
        @NotBlank(message = "Informe a senha atual.")
        String senhaAtual,

        @NotBlank(message = "Informe a nova senha.")
        // mesma regra do cadastro (72 = limite do BCrypt)
        @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres.")
        String novaSenha
) {
    @Override
    public String toString() { // nunca deixar senha aparecer em log
        return "AlterarSenhaRequest[***]";
    }
}
