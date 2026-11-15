package br.com.logicup.backend.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Sprint 10 (decisão D3): o que falta para criar a conta de quem entrou pelo Google pela primeira vez.
 * E-mail e "quem é" NÃO vêm aqui (o cliente poderia mentir): vêm do cookie assinado (GoogleCadastroPendente).
 * Senha também não: a conta entra pelo Google (se quiser uma senha, cria pelo "Esqueci minha senha").
 * As regras são as MESMAS do cadastro normal (CadastroRequest).
 */
public record CompletarCadastroGoogleRequest(
        @NotBlank(message = "O nome é obrigatório.")
        @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
        String nome,

        @NotBlank(message = "O nome de usuário é obrigatório.")
        @Pattern(regexp = CadastroRequest.REGRA_USERNAME,
                message = "Use de 3 a 20 caracteres: letras minúsculas, números, ponto ou sublinhado.")
        String username,

        @NotNull(message = "Escolha o perfil: ALUNO ou PROFESSOR.")
        PerfilCadastro perfil,

        @NotNull(message = "Para criar a conta, aceite os Termos de Uso e confirme a leitura da Política de Privacidade.")
        @AssertTrue(message = "Para criar a conta, aceite os Termos de Uso e confirme a leitura da Política de Privacidade.")
        Boolean aceiteTermos,

        @NotBlank(message = "Versão dos Termos de Uso não informada.")
        String versaoTermos,

        @NotBlank(message = "Versão da Política de Privacidade não informada.")
        String versaoPrivacidade
) {
    public CompletarCadastroGoogleRequest {
        nome = nome == null ? null : nome.trim();
        username = CadastroRequest.normalizarUsername(username);
    }
}
