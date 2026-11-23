package br.com.logicup.backend.security;

import br.com.logicup.backend.exception.ContaBloqueadaException;
import br.com.logicup.backend.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Sprint 11: CORTA NA HORA o acesso de uma conta bloqueada pelo administrador.
 *
 * Por que um filtro? O login já recusa conta bloqueada (AuthService), mas quem JÁ estava logado tem um token
 * (JWT) assinado que continua válido até vencer: o token não "sabe" que a conta foi bloqueada depois.
 * Então, a cada requisição autenticada, este filtro pergunta ao banco se a conta está bloqueada
 * (uma consulta pela chave primária, que devolve só sim/não). Se estiver, responde 401 CONTA_BLOQUEADA:
 * o React trata 401 como "sessão perdida" e volta para a tela de login.
 *
 * Roda DEPOIS da autenticação JWT (precisa saber quem é). Sem login, não faz nada.
 * Não é @Component pelo mesmo motivo dos outros filtros da tarefa 6 (rodaria duas vezes).
 * Custo: uma consulta leve por requisição autenticada; para o tamanho do LogiCup, é desprezível.
 *
 * Sprint 11: a MESMA consulta também diz quando a senha mudou pela última vez. Um token emitido antes disso é
 * de uma sessão antiga (por exemplo, de um aparelho perdido ou de alguém que descobriu a senha) e é recusado
 * com 401 SENHA_TROCADA. Assim, trocar ou redefinir a senha encerra todas as outras sessões.
 */
public class ContaBloqueadaFilter extends OncePerRequestFilter {

    /** Sprint 11: código do erro quando o token é de antes da última troca de senha. */
    public static final String CODIGO_SENHA_TROCADA = "SENHA_TROCADA";

    private final UsuarioRepository usuarioRepository;

    public ContaBloqueadaFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao instanceof JwtAuthenticationToken jwt
                && !"/api/auth/logout".equals(request.getRequestURI())) { // sair sempre funciona
            Integer id = Integer.valueOf(jwt.getToken().getSubject());
            Optional<SituacaoDaConta> situacao = usuarioRepository.situacaoDaConta(id);

            if (situacao.isPresent() && situacao.get().bloqueada()) {
                recusar(response, "Conta bloqueada",
                        "Esta conta está bloqueada. Se achar que é um engano, escreva para contato@logicup.dev.br.",
                        ContaBloqueadaException.CODIGO);
                return;
            }
            // Sprint 11: token emitido antes da última troca de senha = sessão antiga (outro aparelho, ou roubada)
            if (situacao.isPresent() && situacao.get().tokenAnteriorATrocaDeSenha(jwt.getToken().getIssuedAt())) {
                recusar(response, "Sessão encerrada",
                        "A senha desta conta foi trocada. Entre de novo com a senha nova.", CODIGO_SENHA_TROCADA);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private static void recusar(HttpServletResponse response, String titulo, String mensagem, String codigo)
            throws IOException {
        SecurityContextHolder.clearContext();
        RespostaErroJson.escrever(response, 401, titulo, mensagem, codigo);
    }
}
