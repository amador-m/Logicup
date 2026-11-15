package br.com.logicup.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

/**
 * Sprint 10: encerra a sessão NESTE navegador depois de excluir a conta. Faz o mesmo que o logout:
 *  - apaga o cookie de login (httpOnly: só o servidor consegue apagar), com os mesmos atributos da criação;
 *  - apaga os cookies com nomes antigos;
 *  - troca o token CSRF (a "sessão" mudou: o token antigo não deve continuar valendo).
 *
 * E as sessões em OUTROS aparelhos? O token (JWT) delas continua assinado até vencer, mas não serve para
 * nada: todo Service procura o usuário pelo id do token e, como ele não existe mais, responde 401
 * (SessaoInvalidaException) e o React desloga a pessoa.
 *
 * Os cookies são ACRESCENTADOS com addHeader (e não devolvidos no ResponseEntity), pelo mesmo motivo
 * explicado no AuthController.adicionarCookies: senão o cookie CSRF novo sumiria da resposta.
 */
@Component
public class EncerramentoDeSessao {

    private final TokenService tokenService;
    private final CsrfTokenRepository csrfTokenRepository;

    public EncerramentoDeSessao(TokenService tokenService, CsrfTokenRepository csrfTokenRepository) {
        this.tokenService = tokenService;
        this.csrfTokenRepository = csrfTokenRepository;
    }

    public void encerrar(HttpServletRequest request, HttpServletResponse response) {
        csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response);
        response.addHeader(HttpHeaders.SET_COOKIE, tokenService.criarCookieExpirado().toString());
        for (String cookie : CookiesLegados.paraApagar()) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie);
        }
    }
}
