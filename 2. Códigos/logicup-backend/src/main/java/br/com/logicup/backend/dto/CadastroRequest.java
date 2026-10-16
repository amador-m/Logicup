package br.com.logicup.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/**
 * Dados que o frontend envia no cadastro. É um DTO (Data Transfer Object):
 * só contém o que o usuário PODE informar — nada de pontuação, nível, id etc. (evita mass assignment).
 * "record" = classe imutável do Java com construtor, getters, equals e toString gerados.
 */
public record CadastroRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
        String nome,

        // Sprint 7: nome de usuário público (ranking/perfil/login). A unicidade é checada no AuthService.
        @NotBlank(message = "O nome de usuário é obrigatório.")
        @Pattern(regexp = CadastroRequest.REGRA_USERNAME,
                message = "Use de 3 a 20 caracteres: letras minúsculas, números, ponto ou sublinhado.")
        String username,

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
    /** Regra do username: 3 a 20 caracteres, só a-z, 0-9, ponto e sublinhado (a mesma do banco). */
    public static final String REGRA_USERNAME = "^[a-z0-9._]{3,20}$";

    /*
     * Construtor COMPACTO do record: roda assim que o JSON vira objeto, ANTES do @Valid.
     * Tira espaços das pontas para que "  ana@x.com " não seja recusado como e-mail inválido.
     * O username também vira MINÚSCULO: "Ana.Silva" e "ana.silva" são o mesmo username.
     * (a senha NÃO é alterada: espaço pode fazer parte dela)
     */
    public CadastroRequest {
        nome = semEspacosNasPontas(nome);
        username = normalizarUsername(username);
        email = semEspacosNasPontas(email);
    }

    /** "  Ana.Silva " -> "ana.silva". Usado também no login e (futuramente) na edição do perfil. */
    public static String normalizarUsername(String valor) {
        return valor == null ? null : valor.trim().toLowerCase(Locale.ROOT);
    }

    private static String semEspacosNasPontas(String valor) {
        return valor == null ? null : valor.trim();
    }

    @Override
    public String toString() { // nunca deixar a senha aparecer em log
        return "CadastroRequest[nome=" + nome + ", username=" + username + ", email=" + email + ", perfil=" + perfil + "]";
    }
}
