package br.com.logicup.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Diz ao Spring Security ONDE procurar o token.
 * Por padrão ele procura no cabeçalho "Authorization: Bearer ...";
 * no LogicUp o token fica no cookie httpOnly LOGICUP_TOKEN (PE3).
 */
@Component
public class CookieTokenResolver implements BearerTokenResolver {

    /*
     * Nas rotas públicas de autenticação o token é IGNORADO de propósito.
     * Sem isso, um cookie antigo/inválido (ex.: expirado, ou JWT_SECRET trocado) faria até
     * o LOGIN responder 401 — e o usuário ficaria preso, sem conseguir entrar de novo.
     */
    private static final Set<String> ROTAS_SEM_TOKEN = Set.of(
            "/api/auth/csrf", "/api/auth/cadastro", "/api/auth/login", "/api/auth/logout");

    @Override
    public String resolve(HttpServletRequest request) {
        if (ROTAS_SEM_TOKEN.contains(request.getRequestURI())) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null; // sem cookie -> requisição anônima (rotas protegidas respondem 401)
        }
        for (Cookie cookie : cookies) {
            if (TokenService.NOME_COOKIE.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
