package br.com.logicup.backend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Escreve erros 401/403 no MESMO formato do GlobalExceptionHandler.
 * Necessário porque esses erros acontecem nos FILTROS do Spring Security,
 * ANTES de a requisição chegar a um Controller (o @RestControllerAdvice não os vê).
 * As mensagens são constantes nossas (sem aspas), por isso montar o JSON à mão é seguro aqui.
 */
final class RespostaErroJson {

    private RespostaErroJson() {}

    static void escrever(HttpServletResponse response, int status, String erro, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"status\":" + status + ",\"erro\":\"" + erro + "\",\"mensagem\":\"" + mensagem + "\"}");
    }
}
