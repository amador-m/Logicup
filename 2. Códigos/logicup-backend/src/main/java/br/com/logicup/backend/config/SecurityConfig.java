package br.com.logicup.backend.config;

import br.com.logicup.backend.security.CookieTokenResolver;
import br.com.logicup.backend.security.JsonAccessDeniedHandler;
import br.com.logicup.backend.security.JsonAuthenticationEntryPoint;
import br.com.logicup.backend.security.TokenService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuração central de segurança do LogicUp.
 *
 * Toda requisição HTTP passa por uma CADEIA DE FILTROS do Spring Security antes de chegar ao Controller.
 * Aqui dizemos o que cada filtro deve fazer:
 *  - CSRF: exige o token CSRF em POST/PUT/DELETE (PE3);
 *  - sessão: STATELESS — o servidor não guarda sessão; quem prova a identidade é o JWT;
 *  - autorização: quais rotas são públicas e quais exigem login;
 *  - resource server: lê o JWT do cookie, confere assinatura/validade e transforma a "role" em permissão.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final int TAMANHO_MINIMO_SEGREDO = 32; // HS256 exige chave de pelo menos 256 bits (32 bytes)

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CookieTokenResolver cookieTokenResolver,
                                                   JsonAuthenticationEntryPoint entryPoint,
                                                   JsonAccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                // CSRF para SPA: cookie XSRF-TOKEN (legível pelo React) + cabeçalho X-XSRF-TOKEN nas requisições
                .csrf(csrf -> csrf.spa())

                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/cadastro", "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers("/error").permitAll()
                        // PE1: só ALUNO responde exercícios (professor/admin apenas visualizam)
                        .requestMatchers(HttpMethod.POST, "/api/rodadas/*/respostas").hasRole("ALUNO")
                        .anyRequest().authenticated()) // todo o resto exige login (AU5)

                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(cookieTokenResolver)                // token vem do cookie
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDeRoles()))
                        .authenticationEntryPoint(entryPoint)                     // 401 em JSON
                        .accessDeniedHandler(accessDeniedHandler))               // 403 em JSON

                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // mecanismos que NÃO usamos (login por formulário/HTTP Basic/logout padrão do Spring)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }

    /** Converte a claim "role" do token (ex.: "ALUNO") na permissão do Spring "ROLE_ALUNO". */
    private JwtAuthenticationConverter conversorDeRoles() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(TokenService.CLAIM_ROLE);
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(roles);
        return conversor;
    }

    /** Chave secreta usada para ASSINAR e VERIFICAR os tokens (vem da variável JWT_SECRET). */
    @Bean
    public SecretKey chaveJwt(@Value("${logicup.auth.jwt-secret}") String segredo) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
            // falha ao SUBIR a aplicação, com mensagem clara, em vez de falhar no primeiro login
            throw new IllegalStateException(
                    "JWT_SECRET precisa ter pelo menos " + TAMANHO_MINIMO_SEGREDO + " caracteres.");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey chaveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey chaveJwt) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(chaveJwt)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        // além da assinatura, confere a expiração (exp) e se o emissor é "logicup"
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(TokenService.EMISSOR));
        return decoder;
    }

    /** BCrypt: hash lento e com "sal" aleatório, próprio para senhas (AU4). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
