package br.com.logicup.backend.security;

import br.com.logicup.backend.legal.VersoesLegais;
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
    /** Sprint 8: versões dos Termos/Privacidade que o usuário aceitou (ver VersoesLegais.IDENTIFICADOR). */
    public static final String CLAIM_ACEITE = "aceite";
    /*
     * Tarefa 6: prefixo "__Host-". O navegador só aceita um cookie com esse nome se ele vier com
     * Secure, Path=/ e SEM Domain. Na prática: nenhum subdomínio consegue criar ou sobrescrever este cookie.
     */
    public static final String NOME_COOKIE = "__Host-LOGICUP_TOKEN";
    private static final String CAMINHO_COOKIE = "/"; // exigido pelo prefixo __Host-

    private final JwtEncoder jwtEncoder;
    private final Duration validade;

    public TokenService(JwtEncoder jwtEncoder,
                        @Value("${logicup.auth.validade-token}") Duration validade) {
        this.jwtEncoder = jwtEncoder;
        this.validade = validade;
    }

    /** Token SEM aceite (sessão "pendente"). Usado só em testes que simulam um token de antes do aceite. */
    public String gerarToken(Usuario usuario) {
        return gerarToken(usuario, false);
    }

    /**
     * Sprint 8: aceitouVersoesVigentes = true coloca no token a claim "aceite" com as versões VIGENTES.
     * É ela que o AceitePendenteFilter confere (sem consultar o banco a cada requisição).
     * Como o token é ASSINADO, o navegador não consegue inventar essa claim.
     */
    public String gerarToken(Usuario usuario, boolean aceitouVersoesVigentes) {
        Instant agora = Instant.now();
        JwtClaimsSet.Builder construtor = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .subject(String.valueOf(usuario.getId()))
                .claim(CLAIM_ROLE, usuario.getTipoUsuario().name());
        if (aceitouVersoesVigentes) {
            construtor.claim(CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR);
        }
        JwtClaimsSet claims = construtor.build();
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
                .secure(true)          // SEMPRE: exigido pelo __Host- (http://localhost conta como seguro no Chrome/Edge/Firefox)
                .sameSite("Strict")    // não é enviado em requisições vindas de outros sites (CSRF)
                .path(CAMINHO_COOKIE);
    }
}
