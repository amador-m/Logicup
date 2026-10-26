package br.com.logicup.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Tarefa 6: primeira camada contra CSRF. Confere DE ONDE veio uma requisição que altera dados
 * (POST/PUT/PATCH/DELETE), usando os cabeçalhos Sec-Fetch-Site, Origin e Referer (regra em RegrasOrigem).
 *
 * Roda ANTES do CsrfFilter: é a checagem mais barata e barra o ataque antes de qualquer token ser lido.
 * GET/HEAD/OPTIONS não passam por aqui (não alteram dados).
 * Quando não há nenhum desses cabeçalhos (cliente fora do navegador), a requisição segue e o token CSRF
 * continua sendo exigido pelas camadas seguintes.
 *
 * Não é @Component pelo mesmo motivo do CsrfRotasAutenticadasFilter: rodaria duas vezes.
 */
public class OrigemConfiavelFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OrigemConfiavelFilter.class);
    static final String MENSAGEM = "Origem da requisição não permitida.";

    private final RegrasOrigem regrasOrigem;

    public OrigemConfiavelFilter(RegrasOrigem regrasOrigem) {
        this.regrasOrigem = regrasOrigem;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!CsrfFilter.DEFAULT_CSRF_MATCHER.matches(request)) { // leitura: nada a conferir
            filterChain.doFilter(request, response);
            return;
        }

        String secFetchSite = request.getHeader("Sec-Fetch-Site");
        String origin = request.getHeader("Origin");
        RegrasOrigem.Decisao decisao = regrasOrigem.avaliar(secFetchSite, origin, request.getHeader("Referer"));

        if (decisao == RegrasOrigem.Decisao.BLOQUEADA) {
            // registra o suficiente para investigar, sem o Referer completo (pode carregar dados na URL)
            log.warn("Requisição bloqueada pela origem: {} {} Sec-Fetch-Site={} Origin={}",
                    request.getMethod(), request.getRequestURI(), paraLog(secFetchSite), paraLog(origin));
            RespostaErroJson.escrever(response, 403, "Acesso negado", MENSAGEM);
            return; // NÃO continua a cadeia
        }

        filterChain.doFilter(request, response);
    }

    /** Diferencia cabeçalho AUSENTE do valor literal "null" (que o navegador manda em contextos opacos). */
    private static String paraLog(String valor) {
        return valor == null ? "(ausente)" : "\"" + valor + "\"";
    }
}
