package br.com.logicup.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Formato ÚNICO de erro da API (todo erro sai assim, em JSON):
 * {"status": 400, "erro": "Dados inválidos", "mensagem": "...", "campos": {"email": "..."}}
 * "campos" só aparece em erros de validação.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // não envia "campos": null
public record ErroResponse(int status, String erro, String mensagem, Map<String, String> campos) {

    public ErroResponse(int status, String erro, String mensagem) {
        this(status, erro, mensagem, null);
    }
}
