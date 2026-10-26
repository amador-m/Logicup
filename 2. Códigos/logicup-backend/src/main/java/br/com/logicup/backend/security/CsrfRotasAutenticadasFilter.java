package br.com.logicup.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Tarefa 6: exige o token CSRF nas requisições que alteram dados e chegam com o cookie de LOGIN.
 *
 * POR QUE ESTE FILTRO EXISTE
 * O resource server do Spring dispensa o CsrfFilter sempre que o BearerTokenResolver encontra um token
 * (spring-security issue #17959). No LogiCup o token fica num COOKIE, que o navegador envia sozinho,
 * inclusive em requisições disparadas por outro site. Resultado: as rotas logadas ficavam sem CSRF.
 *
 * O QUE ELE FAZ (exatamente a parte que o Spring pula, sem repetir o que o Spring já faz)
 *   - método que altera dados (POST/PUT/PATCH/DELETE)  E  o CookieTokenResolver encontra o cookie de login
 *       -> valida o token aqui;
 *   - qualquer outro caso -> segue adiante; o CsrfFilter do Spring já cuidou (rotas públicas) ou não há
 *     o que validar (GET/HEAD/OPTIONS).
 * Usa o MESMO CookieTokenResolver que o Spring usa para decidir o que ignorar: as duas regras são
 * complementares, então cada requisição que altera dados é validada uma vez, por um dos dois filtros.
 *
 * COMO VALIDA (mesma lógica do CsrfFilter do Spring)
 *   1) pega o token ESPERADO que o CsrfFilter já carregou do CookieCsrfTokenRepository (atributo da
 *      requisição). Se não estiver lá, carrega do mesmo repositório;
 *   2) lê o token ENVIADO no cabeçalho X-XSRF-TOKEN (parâmetro de formulário não é aceito);
 *   3) compara em tempo constante. Ter só o cookie não basta: o navegador anexa cookies sozinho.
 *   Falhou -> 403 em JSON pelo JsonAccessDeniedHandler (mensagem fixa, sem ecoar os tokens).
 *
 * Não é @Component de propósito: se fosse, o Spring Boot também o registraria como filtro comum do
 * servidor e ele rodaria duas vezes (uma fora da cadeia de segurança). É criado no SecurityConfig.
 */
public class CsrfRotasAutenticadasFilter extends OncePerRequestFilter {

    private final CookieTokenResolver cookieTokenResolver;
    private final CsrfTokenRepository csrfTokenRepository;
    private final AccessDeniedHandler accessDeniedHandler;

    public CsrfRotasAutenticadasFilter(CookieTokenResolver cookieTokenResolver,
                                       CsrfTokenRepository csrfTokenRepository,
                                       AccessDeniedHandler accessDeniedHandler) {
        this.cookieTokenResolver = cookieTokenResolver;
        this.csrfTokenRepository = csrfTokenRepository;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        boolean alteraDados = CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request); // tudo menos GET/HEAD/TRACE/OPTIONS
        boolean temCookieDeLogin = cookieTokenResolver.resolve(request) != null;

        if (!alteraDados || !temCookieDeLogin) {
            filterChain.doFilter(request, response);
            return;
        }

        DeferredCsrfToken tokenEsperado = (DeferredCsrfToken) request.getAttribute(DeferredCsrfToken.class.getName());
        if (tokenEsperado == null) { // normalmente o CsrfFilter já carregou; isto é só segurança extra
            tokenEsperado = csrfTokenRepository.loadDeferredToken(request, response);
        }

        CsrfToken esperado = tokenEsperado.get();
        String enviado = request.getHeader(esperado.getHeaderName());

        if (!iguaisEmTempoConstante(esperado.getToken(), enviado)) {
            // mesma distinção do Spring: sem cookie CSRF = "ausente"; cookie e cabeçalho diferentes = "inválido"
            AccessDeniedException erro = tokenEsperado.isGenerated()
                    ? new MissingCsrfTokenException(enviado)
                    : new InvalidCsrfTokenException(esperado, enviado);
            accessDeniedHandler.handle(request, response, erro);
            return; // NÃO continua a cadeia: o Controller nunca é chamado
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Compara sem "parar no primeiro caractere diferente": o tempo de resposta não revela
     * quantos caracteres do token o atacante acertou (ataque de tempo).
     */
    private static boolean iguaisEmTempoConstante(String esperado, String enviado) {
        if (esperado == null || enviado == null) {
            return false;
        }
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8), enviado.getBytes(StandardCharsets.UTF_8));
    }
}
