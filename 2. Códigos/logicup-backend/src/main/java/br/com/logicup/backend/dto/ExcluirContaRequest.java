package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sprint 10: exclusão da própria conta (quem tem senha). O nome do campo é o mesmo da troca de senha
 * (senhaAtual): assim a resposta de senha errada (400 em campos.senhaAtual) é a mesma nas duas telas.
 */
public record ExcluirContaRequest(
        @NotBlank(message = "Informe a sua senha.")
        @Size(max = 72, message = "Senha inválida.")
        String senhaAtual
) {
    @Override
    public String toString() { // nunca deixar senha aparecer em log
        return "ExcluirContaRequest[***]";
    }
}
