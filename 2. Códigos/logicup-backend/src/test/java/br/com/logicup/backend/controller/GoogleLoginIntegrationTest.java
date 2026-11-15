package br.com.logicup.backend.controller;

import br.com.logicup.backend.google.GoogleCadastroPendente;
import br.com.logicup.backend.google.GoogleLoginConfig;
import br.com.logicup.backend.google.GoogleLoginService;
import br.com.logicup.backend.google.IdentidadeGoogle;

import br.com.logicup.backend.email.EmailDeContaSolicitado;
import br.com.logicup.backend.email.TipoEmailConta;
import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 10: "Entrar com Google" (decisões D2 a D5), SEM falar com o Google de verdade.
 *
 * A parte do Google (redirecionar, trocar o código, conferir a assinatura) é do Spring Security, que já é
 * testado pelos autores dele. Aqui testamos o que é NOSSO: o que acontece depois que o Google responde.
 * Para isso, o teste monta a mesma "resposta do Google" (um OidcUser) que o Spring entregaria ao sucessoHandler.
 *
 * As chaves ficam VAZIAS de propósito (properties abaixo): este teste roda com o Google DESLIGADO, mesmo no PC
 * de quem colocou GOOGLE_CLIENT_ID nas variáveis de ambiente. Propriedades do @SpringBootTest valem mais que as
 * variáveis de ambiente, então o resultado não depende de como a máquina está configurada.
 */
@SpringBootTest(properties = {
        "logicup.google.client-id=",
        "logicup.google.client-secret="
})
@AutoConfigureMockMvc
@Transactional
@RecordApplicationEvents
class GoogleLoginIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationEvents eventos;
    @Autowired private GoogleLoginService googleLogin;
    @Autowired private GoogleCadastroPendente pendente;
    @Autowired private TokenService tokenService;
    @Autowired private CsrfTokenRepository csrfTokenRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ---------------- helpers ----------------

    /** O que o Spring entrega depois de conferir a resposta do Google. */
    private static OAuth2AuthenticationToken respostaDoGoogle(String sub, String email, boolean verificado) {
        OidcIdToken idToken = OidcIdToken.withTokenValue("token-de-teste")
                .subject(sub).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60))
                .claims(c -> c.putAll(Map.of("email", email, "email_verified", verificado, "name", "Gabi Google")))
                .build();
        DefaultOidcUser usuario = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("OIDC_USER")), idToken);
        return new OAuth2AuthenticationToken(usuario, usuario.getAuthorities(), "google");
    }

    /** Roda o sucessoHandler de verdade (o mesmo que o Spring chama) e devolve a resposta. */
    private MockHttpServletResponse voltarDoGoogle(String sub, String email, boolean verificado) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        request.getSession(true); // a sessão do "state", que o handler precisa apagar
        MockHttpServletResponse response = new MockHttpServletResponse();
        GoogleLoginConfig.sucesso(googleLogin, pendente, tokenService, csrfTokenRepository)
                .onAuthenticationSuccess(request, response, respostaDoGoogle(sub, email, verificado));
        assertNull(request.getSession(false), "a sessão do OAuth tem que ser apagada na volta");
        return response;
    }

    private static boolean temCookie(MockHttpServletResponse r, String nome) {
        return r.getHeaders("Set-Cookie").stream().anyMatch(c -> c.startsWith(nome + "=") && !c.startsWith(nome + "=;"));
    }

    private static String valorDoCookie(MockHttpServletResponse r, String nome) {
        return r.getHeaders("Set-Cookie").stream().filter(c -> c.startsWith(nome + "="))
                .map(c -> c.substring(nome.length() + 1, c.indexOf(';'))).findFirst().orElseThrow();
    }

    private ResultActions completar(String cookie, String username) throws Exception {
        var requisicao = post("/api/auth/google/completar-cadastro").with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "Gabi Google", "username": "%s", "perfil": "ALUNO", "aceiteTermos": true,
                         "versaoTermos": "%s", "versaoPrivacidade": "%s"}
                        """.formatted(username, VersoesLegais.TERMOS, VersoesLegais.PRIVACIDADE));
        if (cookie != null) requisicao.cookie(new Cookie(GoogleCadastroPendente.NOME_COOKIE, cookie));
        return mockMvc.perform(requisicao);
    }

    // ---------------- pessoa nova (D3) ----------------

    @Test
    void pessoaNovaVaiParaCompletarCadastroEDepoisEntraSemSenha() throws Exception {
        MockHttpServletResponse volta = voltarDoGoogle("sub-nova-1", "it.gg.nova@gmail.com", true);
        assertEquals("/completar-cadastro", volta.getRedirectedUrl());
        assertFalse(temCookie(volta, TokenService.NOME_COOKIE), "ainda não entrou");
        String cookie = valorDoCookie(volta, GoogleCadastroPendente.NOME_COOKIE);

        mockMvc.perform(get("/api/auth/google/pendente").cookie(new Cookie(GoogleCadastroPendente.NOME_COOKIE, cookie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("it.gg.nova@gmail.com"))
                .andExpect(jsonPath("$.nome").value("Gabi Google"));

        var resultado = completar(cookie, "it.gg.nova").andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ALUNO")).andReturn();
        assertNotNull(resultado.getResponse().getCookie(TokenService.NOME_COOKIE), "já sai logada");

        Usuario criado = usuarioRepository.findByUsername("it.gg.nova").orElseThrow();
        assertEquals("sub-nova-1", criado.getGoogleId());
        assertTrue(criado.isEmailConfirmado(), "o Google já confirmou o e-mail");
        assertFalse(criado.temSenha(), "conta do Google não tem senha");
        assertTrue(alunoRepository.existsById(criado.getId()));
        assertEquals(1, eventos.stream(EmailDeContaSolicitado.class)
                .filter(e -> e.tipo() == TipoEmailConta.BOAS_VINDAS).count());
    }

    @Test
    void completarCadastroSemOCookieDoGoogleEhRecusado() throws Exception {
        completar(null, "it.gg.semcookie").andExpect(status().isUnauthorized());
        completar("token-inventado", "it.gg.inventado").andExpect(status().isUnauthorized());
        assertTrue(usuarioRepository.findByUsername("it.gg.semcookie").isEmpty());
    }

    @Test
    void completarCadastroRespeitaUsernameUnico() throws Exception {
        alunoRepository.save(new Aluno("Já existe", "it.gg.ocupado", "it.gg.ocupado@teste.com", "hash"));
        String cookie = pendente.criar(new IdentidadeGoogle("sub-u", "it.gg.u@gmail.com", true, "U"));
        completar(cookie, "it.gg.ocupado").andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.username").exists());
    }

    @Test
    void tokenDoCadastroPendenteNaoServeComoTokenDeLogin() throws Exception {
        String cookie = pendente.criar(new IdentidadeGoogle("sub-x", "it.gg.x@gmail.com", true, "X"));
        mockMvc.perform(get("/api/auth/me").cookie(new Cookie(TokenService.NOME_COOKIE, cookie)))
                .andExpect(status().isUnauthorized()); // outra chave: a assinatura não confere
    }

    // ---------------- conta que já existe (D4) ----------------

    @Test
    void contaConfirmadaComOMesmoEmailEhLigadaAoGoogleEEntra() throws Exception {
        Usuario prof = new Usuario("Prof", "it.gg.prof", "it.gg.prof@gmail.com", passwordEncoder.encode("senhaForte123"),
                TipoUsuario.PROFESSOR);
        prof.confirmarEmail(LocalDateTime.now());
        usuarioRepository.save(prof);

        MockHttpServletResponse volta = voltarDoGoogle("sub-prof", "IT.GG.Prof@gmail.com", true);
        assertEquals("/trilha", volta.getRedirectedUrl());
        assertTrue(temCookie(volta, TokenService.NOME_COOKIE));

        Usuario ligado = usuarioRepository.findByUsername("it.gg.prof").orElseThrow();
        assertEquals("sub-prof", ligado.getGoogleId());
        assertTrue(ligado.temSenha(), "conta confirmada mantém a senha: o dono pode usar os dois jeitos");
    }

    @Test
    void contaNaoConfirmadaComOMesmoEmailPerdeASenhaDeQuemNaoProvouSerDono() throws Exception {
        // alguém cadastrou o e-mail de outra pessoa e nunca confirmou
        usuarioRepository.save(new Usuario("Intruso", "it.gg.intruso", "it.gg.vitima@gmail.com",
                passwordEncoder.encode("senhaDoIntruso1"), TipoUsuario.PROFESSOR));

        voltarDoGoogle("sub-vitima", "it.gg.vitima@gmail.com", true); // a dona verdadeira entra pelo Google

        Usuario conta = usuarioRepository.findByEmail("it.gg.vitima@gmail.com").orElseThrow();
        assertTrue(conta.isEmailConfirmado());
        assertFalse(conta.temSenha(), "a senha do intruso não pode continuar abrindo a conta");
        assertEquals("sub-vitima", conta.getGoogleId());
    }

    @Test
    void contaJaLigadaEntraPeloSubMesmoComOutroEmailNoGoogle() throws Exception {
        Usuario aluno = new Aluno("Ana", "it.gg.ana", "it.gg.ana@gmail.com", null);
        aluno.ligarAoGoogle("sub-ana");
        aluno.confirmarEmail(LocalDateTime.now());
        usuarioRepository.save(aluno);

        MockHttpServletResponse volta = voltarDoGoogle("sub-ana", "it.gg.ana.novo@gmail.com", true);
        assertEquals("/trilha", volta.getRedirectedUrl());
    }

    @Test
    void emailLigadoAOutraContaGoogleEhRecusado() throws Exception {
        Usuario aluno = new Aluno("Bia", "it.gg.bia", "it.gg.bia@gmail.com", null);
        aluno.ligarAoGoogle("sub-bia-original");
        aluno.confirmarEmail(LocalDateTime.now());
        usuarioRepository.save(aluno);

        MockHttpServletResponse volta = voltarDoGoogle("sub-outro", "it.gg.bia@gmail.com", true);
        assertEquals("/login?erro=google_conta_google_diferente", volta.getRedirectedUrl());
        assertFalse(temCookie(volta, TokenService.NOME_COOKIE));
    }

    @Test
    void emailNaoVerificadoPeloGoogleEhRecusado() throws Exception {
        MockHttpServletResponse volta = voltarDoGoogle("sub-nv", "it.gg.nv@gmail.com", false);
        assertEquals("/login?erro=google_email_nao_verificado", volta.getRedirectedUrl());
        assertFalse(temCookie(volta, GoogleCadastroPendente.NOME_COOKIE));
    }

    // ---------------- desligado (sem chave) ----------------

    @Test
    void semChaveDoGoogleOBotaoNaoAparece() throws Exception {
        mockMvc.perform(get("/api/auth/provedores")).andExpect(status().isOk())
                .andExpect(jsonPath("$.google").value(false));
    }
}
