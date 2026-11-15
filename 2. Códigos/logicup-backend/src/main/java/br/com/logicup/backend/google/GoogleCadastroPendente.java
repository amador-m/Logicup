package br.com.logicup.backend.google;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Sprint 10 (decisão D3): guarda, por alguns minutos, "quem o Google disse que é esta pessoa" enquanto ela
 * completa o cadastro (escolhe nome de usuário e perfil e aceita os Termos).
 *
 * Como: um JWT curto (10 min), ASSINADO, num cookie httpOnly (__Host-LOGICUP_GOOGLE). Ninguém consegue
 * inventar ou mudar o e-mail lá dentro sem a chave. Nada vai para o banco antes do cadastro terminar.
 *
 * Por que uma chave DIFERENTE da do login? Este token não pode servir como token de login, nem por engano.
 * A chave é DERIVADA do JWT_SECRET (HMAC do segredo com um rótulo fixo): não há uma variável nova para
 * configurar, e um token deste tipo é recusado pelo validador do login (assinatura não confere).
 */
@Component
public class GoogleCadastroPendente {

    public static final String NOME_COOKIE = "__Host-LOGICUP_GOOGLE";
    static final String EMISSOR = "logicup-google-pendente";
    static final Duration VALIDADE = Duration.ofMinutes(10);

    private final NimbusJwtEncoder encoder;
    private final NimbusJwtDecoder decoder;
    private final Clock clock;

    public GoogleCadastroPendente(@Value("${logicup.auth.jwt-secret}") String segredo, Clock clock) {
        SecretKey chave = derivarChave(segredo);
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(chave));
        this.decoder = NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
        this.decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(EMISSOR)); // confere exp e emissor
        this.clock = clock;
    }

    public String criar(IdentidadeGoogle google) {
        Instant agora = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .issuedAt(agora)
                .expiresAt(agora.plus(VALIDADE))
                .subject(google.sub())
                .claim("email", google.email())
                .claim("nome", google.nome() == null ? "" : google.nome())
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    /** Vazio = sem cookie, assinatura errada, vencido ou de outro tipo. */
    public Optional<IdentidadeGoogle> ler(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Jwt jwt = decoder.decode(token);
            // só chega aqui quem veio do Google COM o e-mail verificado (o handler recusa os outros)
            return Optional.of(new IdentidadeGoogle(jwt.getSubject(), jwt.getClaimAsString("email"), true,
                    jwt.getClaimAsString("nome")));
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public ResponseCookie cookie(String token) {
        return base(token).maxAge(VALIDADE).build();
    }

    public ResponseCookie cookieExpirado() {
        return base("").maxAge(0).build();
    }

    // mesmos atributos do cookie de login (TokenService): __Host- exige Secure, Path=/ e nenhum Domain
    private static ResponseCookie.ResponseCookieBuilder base(String valor) {
        return ResponseCookie.from(NOME_COOKIE, valor).httpOnly(true).secure(true).sameSite("Strict").path("/");
    }

    private static SecretKey derivarChave(String segredo) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] derivada = hmac.doFinal("logicup:cadastro-pendente-google".getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(derivada, "HmacSHA256");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA256 sempre existe na JVM", e);
        }
    }
}
