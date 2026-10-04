package br.com.logicup.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Chamado quando o acesso é NEGADO (role sem permissão ou token CSRF ausente) -> 403 em JSON. */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        String mensagem = (ex instanceof CsrfException)
                ? "Token CSRF ausente ou inválido. Recarregue a página e tente novamente."
                : "Você não tem permissão para acessar este recurso.";
        RespostaErroJson.escrever(response, 403, "Acesso negado", mensagem);
    }
}
