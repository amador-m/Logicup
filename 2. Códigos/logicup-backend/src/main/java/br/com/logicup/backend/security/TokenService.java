package br.com.logicup.backend.security;

import br.com.logicup.backend.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Gera o JWT e monta o cookie que o carrega.
 *
 * Conteúdo do token (claims):
 *   iss  = "logicup"        quem emitiu (conferido na validação)
 *   sub  = id do usuário    QUEM é o usuário
 *   role = ALUNO/PROFESSOR/ADMIN  o que ele pode fazer
 *   iat / exp               quando foi emitido / quando expira
 * O nome NÃO vai no token (pode mudar); o /me busca o valor atual no banco.
 */
@Service
public class TokenService {

    public static final String EMISSOR = "logicup";
    public static final String CLAIM_ROLE = "role";
    public static final String NOME_COOKIE = "LOGICUP_TOKEN";
    private static final String CAMINHO_COOKIE = "/api"; // o navegador só envia o cookie para a API

    private final JwtEncoder jwtEncoder;
    private final Duration validade;
    private final boolean cookieSeguro;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${logicup.auth.validade-token}") Duration validade,
                        @Value("${logicup.auth.cookie-seguro}") boolean cookieSeguro) {
        this.jwtEncoder = jwtEncoder;
        this.validade = validade;
        this.cookieSeguro = cookieSeguro;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_ROLE, usuario.getTipoUsuario().name())
                .build();
        // HS256 = assinatura com chave secreta (HMAC-SHA256). Sem informar o algoritmo, o encoder tentaria RS256.
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }

    /** Cookie de login: o navegador guarda e envia sozinho; o JavaScript não consegue ler (httpOnly). */
    public ResponseCookie criarCookie(String token) {
        return baseCookie(token).maxAge(validade).build();
    }

    /** Cookie "vazio" com validade 0: faz o navegador APAGAR o cookie de login (logout). */
    public ResponseCookie criarCookieExpirado() {
        return baseCookie("").maxAge(0).build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String valor) {
        return ResponseCookie.from(NOME_COOKIE, valor)
                .httpOnly(true)        // inacessível para JavaScript (protege contra roubo via XSS)
                .secure(cookieSeguro)  // só trafega em HTTPS (localhost conta como seguro nos navegadores)
                .sameSite("Strict")    // não é enviado em requisições vindas de outros sites (CSRF)
                .path(CAMINHO_COOKIE);
    }
}
