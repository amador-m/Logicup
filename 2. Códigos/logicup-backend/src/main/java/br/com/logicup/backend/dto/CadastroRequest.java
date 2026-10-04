package br.com.logicup.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados que o frontend envia no cadastro. É um DTO (Data Transfer Object):
 * só contém o que o usuário PODE informar — nada de pontuação, nível, id etc. (evita mass assignment).
 * "record" = classe imutável do Java com construtor, getters, equals e toString gerados.
 */
public record CadastroRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Informe um e-mail válido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        // 72: o BCrypt só considera os primeiros 72 bytes; o limite deixa isso explícito (C4)
        @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres.")
        String senha,

        @NotNull(message = "Escolha o perfil: ALUNO ou PROFESSOR.")
        PerfilCadastro perfil
) {
    /*
     * Construtor COMPACTO do record: roda assim que o JSON vira objeto, ANTES do @Valid.
     * Tira espaços das pontas para que "  ana@x.com " não seja recusado como e-mail inválido.
     * (a senha NÃO é alterada: espaço pode fazer parte dela)
     */
    public CadastroRequest {
        nome = semEspacosNasPontas(nome);
        email = semEspacosNasPontas(email);
    }

    private static String semEspacosNasPontas(String valor) {
        return valor == null ? null : valor.trim();
    }

    @Override
    public String toString() { // nunca deixar a senha aparecer em log
        return "CadastroRequest[nome=" + nome + ", email=" + email + ", perfil=" + perfil + "]";
    }
}
