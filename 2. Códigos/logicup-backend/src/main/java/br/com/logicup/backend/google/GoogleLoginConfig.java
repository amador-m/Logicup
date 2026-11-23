package br.com.logicup.backend.google;

import br.com.logicup.backend.exception.ContaBloqueadaException;
import br.com.logicup.backend.security.CookiesLegados;
import br.com.logicup.backend.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

import java.io.IOException;
import java.util.Locale;

/**
 * Sprint 10 (decisão D2-A): "Entrar com Google" pelo BACKEND, com o Spring Security (OAuth 2.0 + OpenID Connect).
 *
 * O caminho completo:
 *  1. o botão do React leva o navegador para /oauth2/authorization/google;
 *  2. o Spring sorteia um "state" e um "nonce", guarda os dois e redireciona para o Google;
 *  3. a pessoa entra no Google e o Google a devolve para /login/oauth2/code/google?code=...&state=...;
 *  4. o Spring confere o "state" (impede que alguém injete o login de OUTRA pessoa), troca o "code" pelos
 *     tokens direto com o Google (servidor a servidor, com a chave secreta) e confere a ASSINATURA do
 *     ID token do Google;
 *  5. o sucessoHandler (abaixo) chama o GoogleLoginService e manda a pessoa para a trilha ou para
 *     "Complete seu cadastro".
 *
 * Decisão da sessão (opção A): o LogiCup é SEM SESSÃO (quem diz quem você é é o token do cookie). Só nos passos
 * 2 a 4 o Spring precisa lembrar o "state", e para isso usa a sessão HTTP padrão, que é o mecanismo pronto e
 * testado dele. Por isso existe uma SEGUNDA cadeia de filtros, que vale SÓ para /oauth2/** e /login/oauth2/**.
 * A sessão é apagada assim que a volta do Google termina; o resto do sistema continua sem sessão.
 *
 * Só liga se logicup.google.client-id estiver preenchido: sem a chave (no PC de quem não configurou),
 * o site sobe normal e o botão nem aparece (GET /api/auth/provedores).
 */
@Configuration
@ConditionalOnExpression("!'${logicup.google.client-id:}'.isBlank()")
public class GoogleLoginConfig {

    private static final Logger log = LoggerFactory.getLogger(GoogleLoginConfig.class);

    /** O "cadastro" do LogiCup no Google: endereços do Google (prontos no CommonOAuth2Provider) + as nossas chaves. */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            @Value("${logicup.google.client-id}") String clientId,
            @Value("${logicup.google.client-secret:}") String clientSecret) {
        if (clientSecret.isBlank()) {
            throw new IllegalStateException("logicup.google.client-id preenchido, mas falta GOOGLE_CLIENT_SECRET");
        }
        log.info("Entrar com Google: LIGADO");
        return new InMemoryClientRegistrationRepository(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("openid", "email", "profile") // só os básicos: não exigem verificação do Google
                .build());
    }

    /** @Order(1): esta cadeia é consultada ANTES da principal (SecurityConfig), mas só para estes caminhos. */
    @Bean
    @Order(1)
    public SecurityFilterChain googleLoginFilterChain(HttpSecurity http, GoogleLoginService googleLogin,
                                                      GoogleCadastroPendente pendente, TokenService tokenService,
                                                      CsrfTokenRepository csrfTokenRepository) throws Exception {
        http
                .securityMatcher("/oauth2/**", "/login/oauth2/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // só GETs de ida e volta; quem protege contra falsificação aqui é o "state" do OAuth
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // o login do Google NÃO fica guardado na sessão: depois da volta, vale só o nosso token
                .securityContext(c -> c.securityContextRepository(new RequestAttributeSecurityContextRepository()))
                .oauth2Login(oauth -> oauth
                        .successHandler(sucesso(googleLogin, pendente, tokenService, csrfTokenRepository))
                        .failureHandler(falha()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);
        return http.build();
    }

    public static AuthenticationSuccessHandler sucesso(GoogleLoginService googleLogin, GoogleCadastroPendente pendente,
                                                TokenService tokenService, CsrfTokenRepository csrfTokenRepository) {
        return (request, response, autenticacao) -> {
            encerrarSessao(request);
            IdentidadeGoogle google;
            try {
                google = identidade(autenticacao);
            } catch (RuntimeException e) {
                log.warn("Entrar com Google: resposta do Google sem os dados esperados ({})", e.getMessage());
                redirecionar(response, "/login?erro=google");
                return;
            }
            try {
                GoogleLoginService.Resultado resultado = googleLogin.entrar(google);
                if (resultado instanceof GoogleLoginService.Entrou entrou) {
                    // mesma "troca de sessão" do login por senha (AuthController): token CSRF novo + cookie de login
                    csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response);
                    adicionar(response, tokenService.criarCookie(entrou.autenticacao().token()).toString());
                    adicionar(response, pendente.cookieExpirado().toString());
                    adicionar(response, CookiesLegados.paraApagar());
                    redirecionar(response, "/trilha");
                } else {
                    adicionar(response, pendente.cookie(pendente.criar(google)).toString());
                    redirecionar(response, "/completar-cadastro");
                }
            } catch (LoginGoogleRecusadoException e) {
                redirecionar(response, "/login?erro=google_" + e.getMotivo().name().toLowerCase(Locale.ROOT));
            } catch (ContaBloqueadaException e) { // Sprint 11
                redirecionar(response, "/login?erro=conta_bloqueada");
            }
        };
    }

    /** Cancelou na tela do Google, "state" inválido, Google fora do ar...: volta para o login com um aviso. */
    public static AuthenticationFailureHandler falha() {
        return (request, response, excecao) -> {
            log.info("Entrar com Google não concluído: {}", excecao.getMessage());
            encerrarSessao(request);
            redirecionar(response, "/login?erro=google");
        };
    }

    private static IdentidadeGoogle identidade(Authentication autenticacao) {
        OidcUser usuario = (OidcUser) autenticacao.getPrincipal();
        String email = usuario.getEmail();
        if (usuario.getSubject() == null || email == null) {
            throw new IllegalStateException("sem sub ou e-mail");
        }
        return new IdentidadeGoogle(usuario.getSubject(), email.trim().toLowerCase(Locale.ROOT),
                Boolean.TRUE.equals(usuario.getEmailVerified()), usuario.getFullName());
    }

    private static void encerrarSessao(HttpServletRequest request) {
        HttpSession sessao = request.getSession(false);
        if (sessao != null) {
            sessao.invalidate(); // o "state" já foi usado: a sessão não serve para mais nada
        }
    }

    private static void adicionar(HttpServletResponse response, String... cookies) {
        for (String cookie : cookies) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie);
        }
    }

    /** Caminho RELATIVO: o navegador completa com o mesmo endereço (localhost:5173 no PC, logicup.dev.br no ar). */
    private static void redirecionar(HttpServletResponse response, String caminho) throws IOException {
        response.sendRedirect(caminho);
    }
}
