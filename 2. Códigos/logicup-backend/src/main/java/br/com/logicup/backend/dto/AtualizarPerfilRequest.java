package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Sprint 7: o que o aluno PODE mudar no próprio perfil — nome e username.
 * E-mail, senha, tipo de usuário e XP NÃO estão aqui de propósito (evita mass assignment):
 * mesmo que alguém mande "email" ou "pontuacao" no JSON, o Jackson ignora, porque o DTO não tem esses campos.
 * As validações são as MESMAS do cadastro (reaproveita a regra e a normalização do CadastroRequest).
 */
public record AtualizarPerfilRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
        String nome,

        @NotBlank(message = "O nome de usuário é obrigatório.")
        @Pattern(regexp = CadastroRequest.REGRA_USERNAME,
                message = "Use de 3 a 20 caracteres: letras minúsculas, números, ponto ou sublinhado.")
        String username
) {
    /** Mesma normalização do cadastro: nome sem espaços nas pontas; username minúsculo. */
    public AtualizarPerfilRequest {
        nome = nome == null ? null : nome.trim();
        username = CadastroRequest.normalizarUsername(username);
    }
}
