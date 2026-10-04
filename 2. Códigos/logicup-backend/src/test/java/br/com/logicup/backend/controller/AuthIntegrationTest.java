package br.com.logicup.backend.controller;

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
 * csrfValido(): envia cookie XSRF-TOKEN + cabeçalho X-XSRF-TOKEN, como o React fará (ver CsrfTeste).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    private static final String SENHA = "senhaForte123";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;

    private static String jsonCadastro(String nome, String email, String senha, String perfil) {
        return """
                {"nome": "%s", "email": "%s", "senha": "%s", "perfil": "%s"}
                """.formatted(nome, email, senha, perfil);
    }

    private static String jsonLogin(String email, String senha) {
        return """
                {"email": "%s", "senha": "%s"}
                """.formatted(email, senha);
    }

    private MvcResult cadastrar(String email, String perfil) throws Exception {
        return mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCadastro("Pessoa Teste", email, SENHA, perfil)))
                .andReturn();
    }

    // ---------- cadastro ----------

    @Test
    void cadastroDeAlunoCriaContaEJaDeixaLogado() throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCadastro("Aluna Teste", "aluna.auth@teste.com", SENHA, "ALUNO")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ALUNO"))
                .andExpect(jsonPath("$.senha").doesNotExist())   // senha nunca sai na resposta
                .andExpect(jsonPath("$.token").doesNotExist())   // token só no cookie
                .andReturn();

        Cookie cookie = resultado.getResponse().getCookie(TokenService.NOME_COOKIE);
        assertNotNull(cookie, "o cadastro deve devolver o cookie de login (C1)");
        assertTrue(cookie.isHttpOnly());

        // com o cookie, o /me reconhece a pessoa
        mockMvc.perform(get("/api/auth/me").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Aluna Teste"))
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
                        .content(jsonCadastro("Outra Pessoa", "  Repetido@TESTE.com ", SENHA, "PROFESSOR")))
                .andExpect(status().isConflict());
    }

    @Test
    void dadosInvalidosRetornam400ComOsCampos() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonCadastro("A", "nao-e-email", "123", "ALUNO")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
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

    // ---------- login ----------

    @Test
    void loginComDadosCorretosDevolveCookie() throws Exception {
        cadastrar("login.ok@teste.com", "ALUNO");

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
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos."));

        mockMvc.perform(post("/api/auth/login").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("nao.existe@teste.com", "senhaErrada99")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos."));
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
        assertNotNull(resultado.getResponse().getCookie("XSRF-TOKEN"));
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
