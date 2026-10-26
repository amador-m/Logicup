package br.com.logicup.backend.security;

import br.com.logicup.backend.legal.VersoesLegais;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Sprint 8: quem está logado mas NÃO aceitou a versão vigente dos Termos/Privacidade só pode
 * ver quem é (/api/auth/me), sair e aceitar (/api/legal/**). O resto da API responde 403 ACEITE_PENDENTE.
 *
 * O aceite está DENTRO do token (claim "aceite", assinada pelo servidor): este filtro não consulta o banco.
 * Roda DEPOIS da autenticação JWT (precisa saber quem é). Sem login, não faz nada: a autorização
 * do Spring responde 401 como sempre.
 * Não é @Component pelo mesmo motivo dos outros filtros da tarefa 6 (rodaria duas vezes).
 */
public class AceitePendenteFilter extends OncePerRequestFilter {

    static final String CODIGO = "ACEITE_PENDENTE";
    static final String MENSAGEM = "Para continuar, aceite os Termos de Uso e confirme a leitura da Política de Privacidade.";

    /** Rotas liberadas mesmo com aceite pendente. */
    private static final List<String> LIBERADAS = List.of("/api/auth/", "/api/legal/", "/error");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao instanceof JwtAuthenticationToken jwt
                && !VersoesLegais.aceiteVigente(jwt.getToken().getClaimAsString(TokenService.CLAIM_ACEITE))
                && LIBERADAS.stream().noneMatch(request.getRequestURI()::startsWith)) {
            RespostaErroJson.escrever(response, 403, "Aceite pendente", MENSAGEM, CODIGO);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
