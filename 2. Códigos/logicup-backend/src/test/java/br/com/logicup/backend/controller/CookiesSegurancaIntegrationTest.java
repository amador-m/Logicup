package br.com.logicup.backend.controller;

import br.com.logicup.backend.service.AceiteService;
import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.security.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Tarefa 6, etapa 4: ATRIBUTOS dos cookies, troca do token CSRF e limpeza dos cookies antigos.
 *
 * Os testes leem o cabeçalho Set-Cookie BRUTO (exatamente o texto que o navegador recebe),
 * porque é ele que decide se o navegador aceita um cookie "__Host-":
 *   Secure obrigatório, Path=/ obrigatório, Domain proibido.
 * O MockMvc não é um navegador e não aplica essas regras: ele só mostra o que o servidor mandou.
 * Quem prova que o navegador ACEITA é o teste manual da etapa 5.
 *
 * Para não depender do nome antigo nem do novo, o token CSRF é pedido ao servidor (GET /api/auth/csrf)
 * e reenviado com o nome que o servidor usou, como o React faz.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CookiesSegurancaIntegrationTest {

    private static final String COOKIE_LOGIN = "__Host-LOGICUP_TOKEN";
    private static final String COOKIE_CSRF = "__Host-XSRF-TOKEN";
    private static final String COOKIE_LOGIN_ANTIGO = "LOGICUP_TOKEN";
    private static final String COOKIE_CSRF_ANTIGO = "XSRF-TOKEN";
    private static final String CABECALHO_CSRF = "X-XSRF-TOKEN";
    private static final String SENHA = "senhaCookies123";

    @Autowired private MockMvc mockMvc;
    @Autowired private TokenService tokenService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private AceiteService aceiteService;

    private Aluno aluno;

    @BeforeEach
    void criarAluno() {
        aluno = alunoRepository.save(new Aluno("Aluna Cookies", "it.ck.eu", "it.ck.eu@teste.com",
                passwordEncoder.encode(SENHA)));
        aceiteService.registrarVersoesVigentes(aluno); // Sprint 8: aluno que já aceitou os Termos
    }

    // ---------- leitura do Set-Cookie ----------

    /** Um Set-Cookie já "desmontado": nome, valor e atributos (chaves em minúsculas; flags com valor ""). */
    private record SetCookie(String nome, String valor, Map<String, String> atributos) {
        boolean tem(String atributo) {
            return atributos.containsKey(atributo.toLowerCase(Locale.ROOT));
        }

        String atributo(String atributo) {
            return atributos.get(atributo.toLowerCase(Locale.ROOT));
        }

        boolean apaga() {
            return "0".equals(atributo("Max-Age"));
        }
    }

    /** Todos os Set-Cookie da resposta, por nome (se o mesmo nome vier duas vezes, vale o último, como no navegador). */
    private static Map<String, SetCookie> setCookies(MvcResult resultado) {
        Map<String, SetCookie> porNome = new LinkedHashMap<>();
        for (String linha : resultado.getResponse().getHeaders("Set-Cookie")) {
            String[] partes = linha.split(";");
            String[] nomeValor = partes[0].split("=", 2);
            Map<String, String> atributos = new LinkedHashMap<>();
            for (int i = 1; i < partes.length; i++) {
                String[] chaveValor = partes[i].trim().split("=", 2);
                atributos.put(chaveValor[0].trim().toLowerCase(Locale.ROOT), chaveValor.length > 1 ? chaveValor[1].trim() : "");
            }
            porNome.put(nomeValor[0].trim(), new SetCookie(nomeValor[0].trim(), nomeValor.length > 1 ? nomeValor[1] : "", atributos));
        }
        return porNome;
    }

    /** Regras do prefixo __Host-: Secure, Path=/ e sem Domain. */
    private static void respeitaHost(SetCookie cookie) {
        assertTrue(cookie.nome().startsWith("__Host-"), cookie.nome() + ": nome deve ter o prefixo __Host-");
        assertTrue(cookie.tem("Secure"), cookie.nome() + ": __Host- exige Secure");
        assertEquals("/", cookie.atributo("Path"), cookie.nome() + ": __Host- exige Path=/");
        assertFalse(cookie.tem("Domain"), cookie.nome() + ": __Host- proíbe Domain");
    }

    /**
     * SameSite=Strict. Só é conferido aqui no cookie de LOGIN, que nós escrevemos como TEXTO pronto.
     * O cookie CSRF é escrito pelo Spring Security como objeto Cookie do servlet, e quem o transforma
     * em texto é o servidor; o MockMvc não inclui o atributo SameSite nesse texto (o Tomcat inclui).
     * Por isso o SameSite do cookie CSRF é conferido no Tomcat de verdade: CookiesServidorRealIntegrationTest.
     */
    private static void sameSiteStrict(SetCookie cookie) {
        assertEquals("strict", String.valueOf(cookie.atributo("SameSite")).toLowerCase(Locale.ROOT),
                cookie.nome() + ": SameSite deve ser Strict");
    }

    /** Token CSRF como o React obtém: pede ao servidor e usa o nome de cookie que ele devolver. */
    private Cookie pedirTokenCsrf() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/auth/csrf")).andReturn();
        return setCookies(resultado).values().stream()
                .filter(c -> c.nome().endsWith("XSRF-TOKEN") && !c.apaga())
                .map(c -> new Cookie(c.nome(), c.valor()))
                .findFirst().orElseThrow(() -> new AssertionError("o servidor não entregou o cookie CSRF"));
    }

    private MockHttpServletRequestBuilder comCsrf(MockHttpServletRequestBuilder requisicao, Cookie csrf) {
        return requisicao.cookie(csrf).header(CABECALHO_CSRF, csrf.getValue());
    }

    private MvcResult login(Cookie csrf) throws Exception {
        return mockMvc.perform(comCsrf(post("/api/auth/login"), csrf).contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"it.ck.eu\",\"senha\":\"" + SENHA + "\"}")).andReturn();
    }

    /** Os dois cookies do nome antigo precisam ser apagados, com os atributos com que foram criados. */
    private static void limpaCookiesAntigos(Map<String, SetCookie> cookies) {
        SetCookie loginAntigo = cookies.get(COOKIE_LOGIN_ANTIGO);
        assertNotNull(loginAntigo, "deveria apagar o cookie antigo " + COOKIE_LOGIN_ANTIGO);
        assertTrue(loginAntigo.apaga());
        assertEquals("/api", loginAntigo.atributo("Path"), "o antigo foi criado com Path=/api: só apaga com o mesmo Path");

        SetCookie csrfAntigo = cookies.get(COOKIE_CSRF_ANTIGO);
        assertNotNull(csrfAntigo, "deveria apagar o cookie antigo " + COOKIE_CSRF_ANTIGO);
        assertTrue(csrfAntigo.apaga());
        assertEquals("/", csrfAntigo.atributo("Path"));
    }

    // ---------- cookie CSRF ----------

    @Test
    void endpointCsrfEntregaCookieHostLegivelPeloJavaScript() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/auth/csrf")).andReturn();
        SetCookie csrf = setCookies(resultado).get(COOKIE_CSRF);

        assertNotNull(csrf, "esperava o cookie " + COOKIE_CSRF);
        respeitaHost(csrf);
        assertFalse(csrf.tem("HttpOnly"), "o React precisa LER o cookie CSRF para copiá-lo no cabeçalho");
        assertFalse(csrf.valor().isBlank());
    }

    @Test
    void endpointCsrfApagaOsCookiesAntigos() throws Exception {
        limpaCookiesAntigos(setCookies(mockMvc.perform(get("/api/auth/csrf")).andReturn()));
    }

    // ---------- cookie de login ----------

    @Test
    void loginEntregaCookieDeLoginHostHttpOnly() throws Exception {
        MvcResult resultado = login(pedirTokenCsrf());
        assertEquals(200, resultado.getResponse().getStatus());

        SetCookie cookieLogin = setCookies(resultado).get(COOKIE_LOGIN);
        assertNotNull(cookieLogin, "esperava o cookie " + COOKIE_LOGIN);
        respeitaHost(cookieLogin);
        sameSiteStrict(cookieLogin);
        assertTrue(cookieLogin.tem("HttpOnly"), "o JavaScript NÃO pode ler o cookie de login");
        assertTrue(Integer.parseInt(cookieLogin.atributo("Max-Age")) > 0);
    }

    @Test
    void cadastroEntregaCookieDeLoginHost() throws Exception {
        Cookie csrf = pedirTokenCsrf();
        MvcResult resultado = mockMvc.perform(comCsrf(post("/api/auth/cadastro"), csrf)
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"nome":"Aluna Cadastro Cookies","username":"it.ck.novo","email":"it.ck.novo@teste.com",
                         "senha":"%s","perfil":"ALUNO","aceiteTermos":true,"versaoTermos":"%s","versaoPrivacidade":"%s"}""".formatted(SENHA, VersoesLegais.TERMOS, VersoesLegais.PRIVACIDADE))).andReturn();
        assertEquals(201, resultado.getResponse().getStatus());

        SetCookie cookieLogin = setCookies(resultado).get(COOKIE_LOGIN);
        assertNotNull(cookieLogin, "esperava o cookie " + COOKIE_LOGIN);
        respeitaHost(cookieLogin);
        sameSiteStrict(cookieLogin);
        assertTrue(cookieLogin.tem("HttpOnly"));
    }

    @Test
    void logoutApagaCookieDeLoginComOsMesmosAtributos() throws Exception {
        // o navegador só apaga um cookie __Host- se a remoção vier com os MESMOS atributos obrigatórios
        Cookie csrf = pedirTokenCsrf();
        MvcResult resultado = mockMvc.perform(comCsrf(post("/api/auth/logout"), csrf)).andReturn();
        assertEquals(204, resultado.getResponse().getStatus());

        SetCookie remocao = setCookies(resultado).get(COOKIE_LOGIN);
        assertNotNull(remocao, "o logout deveria mandar a remoção de " + COOKIE_LOGIN);
        assertTrue(remocao.apaga(), "Max-Age=0 apaga o cookie");
        respeitaHost(remocao);
        sameSiteStrict(remocao);
        assertTrue(remocao.tem("HttpOnly"));
    }

    // ---------- só o nome NOVO autentica ----------

    @Test
    void cookieDeLoginComONomeNovoAutentica() throws Exception {
        mockMvc.perform(get("/api/auth/me").cookie(new Cookie(COOKIE_LOGIN, tokenService.gerarToken(aluno))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @Test
    void cookieDeLoginComONomeAntigoNaoAutenticaMais() throws Exception {
        mockMvc.perform(get("/api/auth/me").cookie(new Cookie(COOKIE_LOGIN_ANTIGO, tokenService.gerarToken(aluno))))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
    }

    // ---------- troca do token CSRF quando a "sessão" muda ----------

    @Test
    void loginTrocaOTokenCsrf() throws Exception {
        Cookie csrfAntes = pedirTokenCsrf();
        SetCookie csrfDepois = setCookies(login(csrfAntes)).get(COOKIE_CSRF);

        assertNotNull(csrfDepois, "o login deveria entregar um token CSRF novo");
        respeitaHost(csrfDepois);
        assertFalse(csrfDepois.apaga());
        assertNotEquals(csrfAntes.getValue(), csrfDepois.valor(), "o token anterior ao login não pode continuar valendo");
    }

    @Test
    void logoutTrocaOTokenCsrf() throws Exception {
        Cookie csrfAntes = pedirTokenCsrf();
        MvcResult resultado = mockMvc.perform(comCsrf(post("/api/auth/logout"), csrfAntes)).andReturn();
        SetCookie csrfDepois = setCookies(resultado).get(COOKIE_CSRF);

        assertNotNull(csrfDepois, "o logout deveria entregar um token CSRF novo");
        assertFalse(csrfDepois.apaga(), "troca, não apaga: o React precisa de um token para o próximo login");
        assertNotEquals(csrfAntes.getValue(), csrfDepois.valor());
    }

    @Test
    void tokenNovoDoLoginFuncionaNaRequisicaoSeguinte() throws Exception {
        // garante que a troca não quebra o fluxo: o token entregue pelo login é aceito logo depois
        MvcResult resultadoLogin = login(pedirTokenCsrf());
        Map<String, SetCookie> cookies = setCookies(resultadoLogin);
        SetCookie cookieLogin = cookies.get(COOKIE_LOGIN);
        SetCookie csrfNovo = cookies.get(COOKIE_CSRF);
        assertNotNull(cookieLogin);
        assertNotNull(csrfNovo);

        MvcResult perfil = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/perfil")
                .cookie(new Cookie(COOKIE_LOGIN, cookieLogin.valor()), new Cookie(COOKIE_CSRF, csrfNovo.valor()))
                .header(CABECALHO_CSRF, csrfNovo.valor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Aluna Cookies\",\"username\":\"it.ck.eu\"}")).andReturn();
        assertEquals(200, perfil.getResponse().getStatus());
    }

    // ---------- limpeza dos cookies antigos ----------

    @Test
    void loginApagaOsCookiesAntigos() throws Exception {
        limpaCookiesAntigos(setCookies(login(pedirTokenCsrf())));
    }

    @Test
    void logoutApagaOsCookiesAntigos() throws Exception {
        Cookie csrf = pedirTokenCsrf();
        limpaCookiesAntigos(setCookies(mockMvc.perform(comCsrf(post("/api/auth/logout"), csrf)).andReturn()));
    }
}
