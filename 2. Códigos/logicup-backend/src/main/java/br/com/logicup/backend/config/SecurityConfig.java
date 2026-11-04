package br.com.logicup.backend.config;

import br.com.logicup.backend.security.AceitePendenteFilter;
import br.com.logicup.backend.security.CookieTokenResolver;
import br.com.logicup.backend.security.CsrfRotasAutenticadasFilter;
import br.com.logicup.backend.security.JsonAccessDeniedHandler;
import br.com.logicup.backend.security.JsonAuthenticationEntryPoint;
import br.com.logicup.backend.security.OrigemConfiavelFilter;
import br.com.logicup.backend.security.RegrasOrigem;
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
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Configuração central de segurança do LogiCup.
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

    /** Tarefa 6: prefixo __Host- (Secure, Path=/, sem Domain): nenhum subdomínio consegue injetar este cookie. */
    public static final String NOME_COOKIE_CSRF = "__Host-XSRF-TOKEN";

    private static final int TAMANHO_MINIMO_SEGREDO = 32; // HS256 exige chave de pelo menos 256 bits (32 bytes)

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CookieTokenResolver cookieTokenResolver,
                                                   JsonAuthenticationEntryPoint entryPoint,
                                                   JsonAccessDeniedHandler accessDeniedHandler,
                                                   CsrfTokenRepository csrfTokenRepository,
                                                   RegrasOrigem regrasOrigem) throws Exception {
        http
                // CSRF para SPA: cookie __Host-XSRF-TOKEN (legível pelo React) + cabeçalho X-XSRF-TOKEN nas requisições.
                // spa() define o handler de SPA; o repositório é o bean abaixo, o MESMO usado pelo
                // CsrfRotasAutenticadasFilter (um lugar só guarda e lê o token esperado).
                .csrf(csrf -> csrf.spa().csrfTokenRepository(csrfTokenRepository))

                // Tarefa 6, camada 1: origem das requisições que alteram dados, ANTES de qualquer token
                .addFilterBefore(new OrigemConfiavelFilter(regrasOrigem), CsrfFilter.class)

                // Tarefa 6: o resource server dispensa o CsrfFilter quando há cookie de login (issue #17959).
                // Este filtro, logo DEPOIS do CsrfFilter, valida exatamente essas requisições.
                .addFilterAfter(new CsrfRotasAutenticadasFilter(cookieTokenResolver, csrfTokenRepository,
                        accessDeniedHandler), CsrfFilter.class)

                // Sprint 8: logado sem o aceite vigente dos Termos/Privacidade -> só /me, logout e aceite.
                // DEPOIS da autenticação JWT, porque precisa saber quem é (o aceite está dentro do token).
                .addFilterAfter(new AceitePendenteFilter(), BearerTokenAuthenticationFilter.class)

                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/cadastro", "/api/auth/login", "/api/auth/logout",
                                // Sprint 10: links dos e-mails (quem usa ainda não está logado)
                                "/api/auth/confirmar-email", "/api/auth/reenviar-confirmacao",
                                "/api/auth/esqueci-senha", "/api/auth/redefinir-senha").permitAll()
                        .requestMatchers("/error").permitAll()
                        // PE1: só ALUNO responde exercícios (professor/admin apenas visualizam)
                        .requestMatchers(HttpMethod.POST, "/api/rodadas/*/respostas").hasRole("ALUNO")
                        // Sprint 7: a ementa (habilidades de cada questão) é só para professor e admin
                        .requestMatchers("/api/ementa", "/api/ementa/**").hasAnyRole("PROFESSOR", "ADMIN")
                        // Sprint 9 (R1): turmas são do PROFESSOR; "minha turma" é do ALUNO (admin fora — decisão A5)
                        .requestMatchers("/api/turmas", "/api/turmas/**").hasRole("PROFESSOR")
                        .requestMatchers("/api/minha-turma", "/api/minha-turma/**").hasRole("ALUNO")
                        // Sprint 9: conquistas e compra de avatar são do ALUNO (só ele tem progresso e gemas).
                        // O catálogo (GET /api/avatares) e a troca do avatar (PUT /api/perfil/avatar) valem para todos.
                        .requestMatchers("/api/conquistas", "/api/conquistas/**").hasRole("ALUNO")
                        .requestMatchers(HttpMethod.POST, "/api/avatares/*/compra").hasRole("ALUNO")
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

    /**
     * Onde o token CSRF fica guardado: cookie __Host-XSRF-TOKEN, legível pelo JavaScript (é o que o React
     * devolve no cabeçalho X-XSRF-TOKEN). É um bean para ser COMPARTILHADO entre o CsrfFilter do
     * Spring, o CsrfRotasAutenticadasFilter e o AuthController (que troca o token no login/logout).
     *
     * Atributos FIXOS, sem depender de request.isSecure() (que é false atrás de um proxy HTTPS):
     *   Secure + Path=/ + sem Domain -> exigidos pelo __Host-;  SameSite=Strict -> não vai em pedidos de outro site.
     * O customizer roda por último, depois dos valores padrão do Spring, e vale também para a REMOÇÃO.
     */
    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repositorio = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repositorio.setCookieName(NOME_COOKIE_CSRF);
        repositorio.setCookiePath("/");
        repositorio.setCookieCustomizer(cookie -> cookie.secure(true).sameSite("Strict"));
        return repositorio;
    }

    /**
     * Tarefa 6: origens confiáveis (esquema://host[:porta], separadas por vírgula).
     * A lista é validada aqui, na subida: vazia, com curinga, "null" ou caminho -> a aplicação não sobe.
     */
    @Bean
    public RegrasOrigem regrasOrigem(@Value("${logicup.seguranca.origens-confiaveis}") List<String> origensConfiaveis) {
        return new RegrasOrigem(origensConfiaveis);
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
