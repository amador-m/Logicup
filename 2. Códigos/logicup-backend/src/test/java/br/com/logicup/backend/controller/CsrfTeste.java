package br.com.logicup.backend.controller;

import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * CSRF nos testes, do MESMO jeito que o navegador faz com o LogicUp:
 * manda o cookie XSRF-TOKEN e o cabeçalho X-XSRF-TOKEN com o mesmo valor ("double submit cookie").
 *
 * Por que não usar o csrf() do spring-security-test?
 * Ele TROCA o repositório de tokens do CsrfFilter por um que guarda o token na sessão HTTP.
 * Como o Spring reaproveita o mesmo contexto entre as classes de teste, essa troca "vaza"
 * para os outros testes: depois dela, GET /api/auth/csrf deixava de devolver o cookie.
 * Este helper não mexe em nada da configuração e ainda testa o mecanismo real.
 */
final class CsrfTeste {

    private static final String TOKEN = "token-csrf-de-teste";

    private CsrfTeste() {}

    static RequestPostProcessor csrfValido() {
        return request -> {
            Cookie[] existentes = request.getCookies() != null ? request.getCookies() : new Cookie[0];
            Cookie[] cookies = java.util.Arrays.copyOf(existentes, existentes.length + 1);
            cookies[existentes.length] = new Cookie("XSRF-TOKEN", TOKEN);
            request.setCookies(cookies);
            request.addHeader("X-XSRF-TOKEN", TOKEN);
            return request;
        };
    }
}
