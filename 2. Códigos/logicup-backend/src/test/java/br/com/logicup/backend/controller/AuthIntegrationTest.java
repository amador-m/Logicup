package br.com.logicup.backend.controller;

import br.com.logicup.backend.email.TipoEmailConta;
import br.com.logicup.backend.legal.VersoesLegais;
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
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de INTEGRAÇÃO: sobe a aplicação inteira (segurança, controllers, services, banco real)
 * e faz requisições HTTP simuladas com o MockMvc — sem abrir porta de rede.
 * @Transactional: cada teste é desfeito no final, então os usuários criados aqui não ficam no banco.
 * csrfValido(): envia cookie __Host-XSRF-TOKEN + cabeçalho X-XSRF-TOKEN, como o React faz (ver CsrfTeste).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RecordApplicationEvents // Sprint 10: para ler o código do link de confirmação
class AuthIntegrationTest {

    private static final String SENHA = "senhaForte123";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private ApplicationEvents eventos;

    // usernames dos testes começam com "it." para não colidir com usuários reais do banco
    private static String jsonCadastro(String nome, String username, String email, String senha, String perfil) {
        return """
                {"nome": "%s", "username": "%s", "email": "%s", "senha": "%s", "perfil": "%s",
                 "aceiteTermos": true, "versaoTermos": "%s", "versaoPrivacidade": "%s"}
                """.formatted(nome, username, email, senha, perfil, VersoesLegais.TERMOS, VersoesLegais.PRIVACIDADE);
    }

    // Sprint 7: o campo do login agora se chama "login" (aceita e-mail OU username)
    private static String jsonLogin(String login, String senha) {
        return """
                {"login": "%s", "senha": "%s"}
                """.formatted(login, senha);
    }

    /** Cadastro com username gerado a partir do e-mail: "login.ok@teste.com" -> "it.login.ok". */
    private MvcResult cadastrar(String email, String perfil) throws Exception {
        String username = "it." + email.substring(0, email.indexOf('@'));
        return mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCadastro("Pessoa Teste", username, email, SENHA, perfil)))
                .andReturn();
    }

    /** Sprint 10: cadastro + abrir o link de confirmação (agora é assim que a conta fica pronta para entrar). */
    private Cookie cadastrarEConfirmar(String email) throws Exception {
        assertEquals(201, cadastrar(email, "ALUNO").getResponse().getStatus());
        return ContasDeTeste.confirmarCadastro(mockMvc, eventos, email);
    }

    // ---------- cadastro ----------

    @Test
    void cadastroDeAlunoCriaContaPendenteEOLinkDeixaLogado() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCadastro("Aluna Teste", "it.aluna.auth", "aluna.auth@teste.com", SENHA, "ALUNO")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("aluna.auth@teste.com"))
                .andExpect(jsonPath("$.senha").doesNotExist())   // senha nunca sai na resposta
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();

        // Sprint 10 (decisão C): conta criada, mas sem login e com o e-mail ainda não confirmado
        assertNull(resultado.getResponse().getCookie(TokenService.NOME_COOKIE), "o cadastro não pode deixar logado");
        assertFalse(usuarioRepository.findByEmail("aluna.auth@teste.com").orElseThrow().isEmailConfirmado());
        assertEquals(1, ContasDeTeste.pedidos(eventos, TipoEmailConta.CONFIRMAR_EMAIL, "aluna.auth@teste.com").size(),
                "o cadastro pede UM e-mail de confirmação");

        // o link do e-mail confirma e já entrega o cookie de login
        Cookie cookie = ContasDeTeste.confirmarCadastro(mockMvc, eventos, "aluna.auth@teste.com");
        assertTrue(cookie.isHttpOnly());
        assertTrue(usuarioRepository.findByEmail("aluna.auth@teste.com").orElseThrow().isEmailConfirmado());
        assertEquals(1, ContasDeTeste.pedidos(eventos, TipoEmailConta.BOAS_VINDAS, "aluna.auth@teste.com").size(),
                "confirmar pede o e-mail de boas-vindas (D1-A)");

        // com o cookie, o /me reconhece a pessoa
        mockMvc.perform(get("/api/auth/me").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Aluna Teste"))
                .andExpect(jsonPath("$.username").value("it.aluna.auth"))
                .andExpect(jsonPath("$.email").value("aluna.auth@teste.com"));

        Integer id = usuarioRepository.findByEmail("aluna.auth@teste.com").orElseThrow().getId();
        assertTrue(alunoRepository.existsById(id), "ALUNO precisa da linha na tabela aluno");
    }

    @Test
    void cadastroDeProfessorNaoCriaLinhaDeAluno() throws Exception {
        MvcResult resultado = cadastrar("prof.auth@teste.com", "PROFESSOR");
        assertEquals(201, resultado.getResponse().getStatus());

        Usuario professor = usuarioRepository.findByEmail("prof.auth@teste.com").orElseThrow();
        assertFalse(alunoRepository.existsById(professor.getId()));
    }

    @Test
    void cadastroComoAdminEhRecusado() throws Exception {
        MvcResult resultado = cadastrar("admin.falso@teste.com", "ADMIN");

        assertEquals(400, resultado.getResponse().getStatus());
        assertTrue(usuarioRepository.findByEmail("admin.falso@teste.com").isEmpty());
    }

    @Test
    void emailRepetidoEhRecusadoMesmoComMaiusculas() throws Exception {
        cadastrar("repetido@teste.com", "ALUNO");

        mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCadastro("Outra Pessoa", "it.outra.pessoa", "  Repetido@TESTE.com ", SENHA, "PROFESSOR")))
                .andExpect(status().isConflict());
    }

    @Test
    void dadosInvalidosRetornam400ComOsCampos() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCadastro("A", "a b", "nao-e-email", "123", "ALUNO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.username").exists())
                .andExpect(jsonPath("$.campos.email").exists())
                .andExpect(jsonPath("$.campos.senha").exists());
    }

    @Test
    void senhaEhGravadaComBcrypt() throws Exception {
        cadastrar("bcrypt@teste.com", "ALUNO");

        String gravada = usuarioRepository.findByEmail("bcrypt@teste.com").orElseThrow().getSenha();
        assertNotEquals(SENHA, gravada);
        assertTrue(gravada.startsWith("$2"), "hash BCrypt começa com $2a$/$2b$");
    }

    // ---------- username (Sprint 7) ----------

    @Test
    void usernameEhObrigatorio() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Sem Username", "email": "sem.username@teste.com", "senha": "%s", "perfil": "ALUNO"}
                                """.formatted(SENHA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.username").exists());
    }

    @Test
    void usernameForaDaRegraEhRecusado() throws Exception {
        // curto demais, com caractere proibido (hífen e @) e longo demais
        for (String invalido : new String[]{"ab", "maria-silva", "maria@silva", "um.username.longo.demais"}) {
            mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonCadastro("Pessoa", invalido, "regra." + invalido.length() + "@teste.com", SENHA, "ALUNO")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.username").exists());
        }
    }

    @Test
    void usernameEhGravadoEmMinusculas() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCadastro("Pessoa", "  IT.Maiusculo ", "maiusculo@teste.com", SENHA, "ALUNO")))
                .andExpect(status().isCreated());
        assertTrue(usuarioRepository.findByUsername("it.maiusculo").isPresent());
    }

    @Test
    void usernameRepetidoRetorna409NoCampo() throws Exception {
        cadastrar("dono.username@teste.com", "ALUNO"); // username "it.dono.username"

        mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCadastro("Outra", "IT.Dono.Username", "outro.email@teste.com", SENHA, "ALUNO")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.username").exists());

        assertTrue(usuarioRepository.findByEmail("outro.email@teste.com").isEmpty(), "nada pode ser gravado");
    }

    // ---------- login ----------

    @Test
    void loginPeloUsernameFunciona() throws Exception {
        cadastrarEConfirmar("login.username@teste.com"); // username "it.login.username"

        MvcResult resultado = mockMvc.perform(post("/api/auth/login").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("  IT.Login.Username ", SENHA))) // normalizado como no cadastro
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("login.username@teste.com"))
                .andReturn();

        assertNotNull(resultado.getResponse().getCookie(TokenService.NOME_COOKIE));
    }

    @Test
    void usernameInexistenteTemAMesmaMensagemDeSenhaErrada() throws Exception {
        mockMvc.perform(post("/api/auth/login").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("it.ninguem", "senhaErrada99")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos."));
    }

    @Test
    void loginComDadosCorretosDevolveCookie() throws Exception {
        cadastrarEConfirmar("login.ok@teste.com");

        MvcResult resultado = mockMvc.perform(post("/api/auth/login").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("LOGIN.ok@teste.com", SENHA))) // e-mail normalizado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andReturn();

        assertNotNull(resultado.getResponse().getCookie(TokenService.NOME_COOKIE));
    }

    @Test
    void senhaErradaEEmailInexistenteTemAMesmaMensagem() throws Exception {
        cadastrar("login.erro@teste.com", "ALUNO");

        mockMvc.perform(post("/api/auth/login").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("login.erro@teste.com", "senhaErrada99")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos."));

        mockMvc.perform(post("/api/auth/login").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("nao.existe@teste.com", "senhaErrada99")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos."));
    }

    // ---------- proteção ----------

    @Test
    void meSemCookieRetorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void cookieAdulteradoRetorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me").cookie(new Cookie(TokenService.NOME_COOKIE, "token.falso.123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postSemTokenCsrfRetorna403() throws Exception {
        mockMvc.perform(post("/api/auth/login") // sem .with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("qualquer@teste.com", SENHA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointCsrfDevolveCookieXsrf() throws Exception {
        MvcResult resultado = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isNoContent())
                .andReturn();
        assertNotNull(resultado.getResponse().getCookie(CsrfTeste.NOME_COOKIE_CSRF));
    }

    // ---------- logout ----------

    @Test
    void logoutApagaOCookie() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/logout").with(csrfValido()))
                .andExpect(status().isNoContent())
                .andReturn();

        Cookie cookie = resultado.getResponse().getCookie(TokenService.NOME_COOKIE);
        assertNotNull(cookie);
        assertEquals(0, cookie.getMaxAge()); // validade 0 = navegador apaga
    }
}
