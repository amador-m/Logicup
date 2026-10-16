package br.com.logicup.backend.controller;

import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 7: teste de INTEGRAÇÃO da edição de perfil (PUT /api/perfil e PUT /api/perfil/senha).
 * Banco real; tudo é desfeito no fim (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PerfilIntegrationTest {

    private static final String SENHA = "senhaAtual123";

    @Autowired private MockMvc mockMvc;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Aluno eu;

    @BeforeEach
    void criarAluno() {
        eu = alunoRepository.save(new Aluno("Aluna Perfil", "it.pf.eu", "it.pf.eu@teste.com",
                passwordEncoder.encode(SENHA)));
        alunoRepository.save(new Aluno("Outra", "it.pf.outra", "it.pf.outra@teste.com", "hash"));
    }

    private static RequestPostProcessor logadoComo(Integer id) {
        return jwt().jwt(token -> token.subject(String.valueOf(id)).claim("role", "ALUNO"))
                .authorities(new SimpleGrantedAuthority("ROLE_ALUNO"));
    }

    private ResultActions putJson(String url, String json, boolean logado) throws Exception {
        var requisicao = put(url).with(csrfValido()).contentType(MediaType.APPLICATION_JSON).content(json);
        if (logado) requisicao = requisicao.with(logadoComo(eu.getId()));
        return mockMvc.perform(requisicao);
    }

    // ---------- PUT /api/perfil ----------

    @Test
    void atualizaNomeEUsernameNormalizado() throws Exception {
        putJson("/api/perfil", "{\"nome\":\"  Maria Nova \",\"username\":\" It.Pf.Nova \"}", true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Nova"))
                .andExpect(jsonPath("$.username").value("it.pf.nova"))
                .andExpect(jsonPath("$.email").value("it.pf.eu@teste.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        Usuario salvo = usuarioRepository.findById(eu.getId()).orElseThrow();
        assertEquals("it.pf.nova", salvo.getUsername());
    }

    @Test
    void manterOProprioUsernameNaoEConflito() throws Exception {
        putJson("/api/perfil", "{\"nome\":\"Só o nome mudou\",\"username\":\"it.pf.eu\"}", true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Só o nome mudou"));
    }

    @Test
    void usernameDeOutraPessoaDa409NoCampo() throws Exception {
        putJson("/api/perfil", "{\"nome\":\"Aluna\",\"username\":\"it.pf.outra\"}", true)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.username").exists());
    }

    @Test
    void usernameForaDaRegraDa400() throws Exception {
        putJson("/api/perfil", "{\"nome\":\"Aluna\",\"username\":\"com espaço!\"}", true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.username").exists());
    }

    @Test
    void emailEnviadoNoJsonEIgnorado() throws Exception {
        putJson("/api/perfil", "{\"nome\":\"Aluna\",\"username\":\"it.pf.eu\",\"email\":\"hacker@x.com\"}", true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("it.pf.eu@teste.com"));
    }

    @Test
    void perfilExigeLogin() throws Exception {
        putJson("/api/perfil", "{\"nome\":\"Aluna\",\"username\":\"it.pf.x\"}", false)
                .andExpect(status().isUnauthorized());
    }

    /*
     * NÃO há teste "PUT sem CSRF devolve 403" aqui, de propósito:
     *  1) o jwt() do spring-security-test chama CsrfFilter.skipRequest(), ou seja, desliga o CSRF na requisição;
     *  2) no Spring, o resource server dispensa o CSRF quando a requisição traz o token de login.
     *     Para rotas logadas, a proteção contra CSRF é o cookie LOGICUP_TOKEN com SameSite=Strict
     *     (o navegador não envia o cookie em requisições vindas de outro site).
     * O CSRF das rotas públicas (login/cadastro) continua testado no AuthIntegrationTest.
     */

    // ---------- PUT /api/perfil/senha ----------

    @Test
    void trocaASenhaComASenhaAtualCorreta() throws Exception {
        putJson("/api/perfil/senha", "{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"novaSenha456\"}", true)
                .andExpect(status().isNoContent());

        String hash = usuarioRepository.findById(eu.getId()).orElseThrow().getSenha();
        assertTrue(passwordEncoder.matches("novaSenha456", hash)); // gravou o HASH da nova senha
    }

    @Test
    void senhaAtualErradaDa400NoCampoENao401() throws Exception {
        putJson("/api/perfil/senha", "{\"senhaAtual\":\"errada000\",\"novaSenha\":\"novaSenha456\"}", true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senhaAtual").exists());

        String hash = usuarioRepository.findById(eu.getId()).orElseThrow().getSenha();
        assertTrue(passwordEncoder.matches(SENHA, hash)); // nada mudou
    }

    @Test
    void novaSenhaCurtaDa400() throws Exception {
        putJson("/api/perfil/senha", "{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"curta\"}", true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.novaSenha").exists());
    }

    @Test
    void novaSenhaIgualAAtualDa400() throws Exception {
        putJson("/api/perfil/senha", "{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"" + SENHA + "\"}", true)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.novaSenha").exists());
    }
}