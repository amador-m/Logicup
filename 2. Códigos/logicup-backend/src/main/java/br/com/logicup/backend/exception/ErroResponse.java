package br.com.logicup.backend.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Formato ÚNICO de erro da API (todo erro sai assim, em JSON):
 * {"status": 400, "erro": "Dados inválidos", "mensagem": "...", "campos": {"email": "..."}}
 * "campos" só aparece em erros de validação; "codigo" só em erros que o frontend precisa reconhecer.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // não envia "campos": null
public record ErroResponse(int status, String erro, String mensagem, Map<String, String> campos, String codigo) {

    public ErroResponse(int status, String erro, String mensagem) {
        this(status, erro, mensagem, null, null);
    }

    public ErroResponse(int status, String erro, String mensagem, Map<String, String> campos) {
        this(status, erro, mensagem, campos, null);
    }

    /**
     * Sprint 10: erro com "codigo" (ex.: EMAIL_NAO_CONFIRMADO), para o frontend decidir pelo código e não
     * pelo texto da mensagem — o mesmo formato que o RespostaErroJson já usa no ACEITE_PENDENTE.
     */
    public static ErroResponse comCodigo(int status, String erro, String mensagem, String codigo) {
        return new ErroResponse(status, erro, mensagem, null, codigo);
    }
}
