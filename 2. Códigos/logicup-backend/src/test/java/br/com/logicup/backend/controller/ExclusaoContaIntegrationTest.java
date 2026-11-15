package br.com.logicup.backend.controller;

import br.com.logicup.backend.email.TipoEmailConta;
import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.AlunoTurma;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Turma;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.AlunoTurmaRepository;
import br.com.logicup.backend.repository.TurmaRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import br.com.logicup.backend.service.AceiteService;
import br.com.logicup.backend.service.TokenUsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 10 (Etapa 4): a pessoa exclui a PRÓPRIA conta.
 *
 * O teste mais importante é o primeiro: ele confere, direto nas tabelas (JdbcTemplate), que o ON DELETE
 * CASCADE do banco apagou TUDO o que era da pessoa (progresso, avatares, conquistas, turmas, aceites,
 * códigos de e-mail) e que a turma do professor continua existindo.
 * Como a classe é @Transactional, tudo é desfeito no fim (o banco de testes volta a ser como era).
 * O limite de pedidos de link (3 por hora por e-mail) é o mesmo dos outros e-mails, já testado no
 * LimiteTentativasIntegrationTest (aqui os limites ficam altos: ver test/resources/config).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RecordApplicationEvents
class ExclusaoContaIntegrationTest {

    private static final String SENHA = "senhaForte123";

    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationEvents eventos;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private TurmaRepository turmaRepository;
    @Autowired private AlunoTurmaRepository alunoTurmaRepository;
    @Autowired private AceiteService aceiteService;
    @Autowired private TokenUsuarioService tokenUsuarioService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private TokenService tokenService;

    // ---------------- helpers ----------------

    private Aluno aluno(String username, boolean comSenha) {
        Aluno aluno = new Aluno("Aluna Exclusão", username, username + "@teste.com",
                comSenha ? passwordEncoder.encode(SENHA) : null);
        if (!comSenha) aluno.ligarAoGoogle("sub-" + username);
        return alunoRepository.save(ContasDeTeste.confirmada(aluno));
    }

    private Usuario usuario(String username, TipoUsuario tipo) {
        return usuarioRepository.save(ContasDeTeste.confirmada(new Usuario("Pessoa " + tipo, username,
                username + "@teste.com", passwordEncoder.encode(SENHA), tipo)));
    }

    private static RequestPostProcessor como(Usuario usuario) {
        String papel = usuario.getTipoUsuario().name();
        return jwt().jwt(t -> t.subject(String.valueOf(usuario.getId())).claim("role", papel)
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_" + papel));
    }

    private ResultActions excluirComSenha(Usuario usuario, String senha) throws Exception {
        return mockMvc.perform(post("/api/perfil/exclusao").with(como(usuario)).with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content("{\"senhaAtual\": \"" + senha + "\"}"));
    }

    private ResultActions pedirLink(Usuario usuario) throws Exception {
        return mockMvc.perform(post("/api/perfil/exclusao/link").with(como(usuario)).with(csrfValido()));
    }

    private ResultActions abrirLink(String codigo) throws Exception {
        return mockMvc.perform(post("/api/auth/excluir-conta").with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content("{\"token\": \"" + codigo + "\"}"));
    }

    private int linhas(String tabela, Integer idUsuario) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tabela + " WHERE id_usuario = ?", Integer.class, idUsuario);
    }

    private boolean existe(Integer idUsuario) {
        entityManager.clear(); // relê do banco, e não da memória do Hibernate
        return usuarioRepository.existsById(idUsuario);
    }

    private static boolean apagouCookieDeLogin(MockHttpServletResponse resposta) {
        return resposta.getHeaders("Set-Cookie").stream()
                .anyMatch(c -> c.startsWith(TokenService.NOME_COOKIE + "=;") && c.contains("Max-Age=0"));
    }

    // ---------------- com senha ----------------

    @Test
    void alunoExcluiComASenhaEOBancoApagaTudoQueEraDele() throws Exception {
        Usuario professor = usuario("it.ex.prof", TipoUsuario.PROFESSOR);
        Turma turma = turmaRepository.save(new Turma("Turma da exclusão", professor, "EXCL01"));
        Aluno aluno = aluno("it.ex.aluno", true);
        Integer id = aluno.getId();

        // um pouco de tudo que pertence a um aluno
        alunoTurmaRepository.save(new AlunoTurma(aluno, turma));
        aceiteService.registrarVersoesVigentes(aluno);
        tokenUsuarioService.gerar(aluno, br.com.logicup.backend.model.FinalidadeToken.REDEFINIR_SENHA);
        Integer exercicio = jdbc.queryForObject("SELECT MIN(id_exercicio) FROM exercicio", Integer.class);
        jdbc.update("INSERT INTO progresso_exercicio (id_usuario, id_exercicio, status, tentativas, pontos_ganhos) "
                + "VALUES (?, ?, 'CONCLUIDO', 1, 10)", id, exercicio);
        jdbc.update("INSERT INTO aluno_avatar (id_usuario, codigo_avatar, preco_pago) VALUES (?, 'GATO_ESCURO', 0)", id);
        jdbc.update("INSERT INTO aluno_conquista (id_usuario, codigo_conquista, nivel, gemas_recebidas) "
                + "VALUES (?, 'PRIMEIRO_ACERTO', 'UNICA', 5)", id);
        entityManager.flush();
        assertTrue(linhas("progresso_exercicio", id) > 0 && linhas("aceite_documento", id) > 0);

        MockHttpServletResponse resposta = excluirComSenha(aluno, SENHA)
                .andExpect(status().isNoContent()).andReturn().getResponse();

        assertFalse(existe(id));
        for (String tabela : new String[]{"usuario", "aluno", "progresso_exercicio", "aluno_avatar",
                "aluno_conquista", "aluno_turma", "aceite_documento", "token_usuario"}) {
            assertEquals(0, linhas(tabela, id), "sobrou linha em " + tabela);
        }
        assertTrue(turmaRepository.existsById(turma.getId()), "a turma do professor continua existindo");
        assertTrue(apagouCookieDeLogin(resposta), "a resposta tem que apagar o cookie de login");
        assertEquals(1, ContasDeTeste.pedidos(eventos, TipoEmailConta.CONTA_EXCLUIDA, "it.ex.aluno@teste.com").size());
    }

    @Test
    void senhaErradaNaoExcluiENaoDesloga() throws Exception {
        Aluno aluno = aluno("it.ex.errada", true);

        excluirComSenha(aluno, "senhaErrada99")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.senhaAtual").exists());

        assertTrue(existe(aluno.getId()));
        assertTrue(ContasDeTeste.pedidos(eventos, TipoEmailConta.CONTA_EXCLUIDA, "it.ex.errada@teste.com").isEmpty());
    }

    @Test
    void depoisDeExcluirOTokenQueSobrouEmOutroAparelhoRecebe401() throws Exception {
        Aluno aluno = aluno("it.ex.outro", true);
        excluirComSenha(aluno, SENHA).andExpect(status().isNoContent());

        // o mesmo JWT (como se estivesse no celular) ainda é bem assinado, mas a conta não existe mais
        mockMvc.perform(get("/api/auth/me").with(como(aluno))).andExpect(status().isUnauthorized());
    }

    @Test
    void comOCookieDeLoginDeVerdadeSemCsrfNaoExclui() throws Exception {
        // com o cookie de login (como no navegador), o token CSRF é obrigatório: um site malicioso
        // não consegue fazer o navegador da pessoa excluir a conta dela
        Aluno aluno = aluno("it.ex.csrf", true);
        Cookie cookieDeLogin = new Cookie(TokenService.NOME_COOKIE, tokenService.gerarToken(aluno, true));
        mockMvc.perform(post("/api/perfil/exclusao").cookie(cookieDeLogin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"senhaAtual\": \"" + SENHA + "\"}"))
                .andExpect(status().isForbidden());
        assertTrue(existe(aluno.getId()));
    }

    // ---------------- quem não pode ----------------

    @Test
    void professorComTurmaNaoExcluiMasSemTurmaExclui() throws Exception {
        Usuario professor = usuario("it.ex.prof2", TipoUsuario.PROFESSOR);
        Turma turma = turmaRepository.save(new Turma("Turma que segura", professor, "EXCL02"));

        excluirComSenha(professor, SENHA)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("1 turma")));
        assertTrue(existe(professor.getId()));

        turmaRepository.delete(turma); // apagou a turma: agora pode
        entityManager.flush();
        excluirComSenha(professor, SENHA).andExpect(status().isNoContent());
        assertFalse(existe(professor.getId()));
    }

    @Test
    void adminNaoExcluiPeloPerfil() throws Exception {
        Usuario admin = usuario("it.ex.admin", TipoUsuario.ADMIN);
        excluirComSenha(admin, SENHA)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("administrador")));
        assertTrue(existe(admin.getId()));
    }

    // ---------------- conta só do Google (link por e-mail) ----------------

    @Test
    void contaDoGoogleExcluiPeloLinkDoEmailQueSoFuncionaUmaVez() throws Exception {
        Aluno aluno = aluno("it.ex.google", false);
        mockMvc.perform(get("/api/auth/me").with(como(aluno))).andExpect(jsonPath("$.temSenha").value(false));

        // sem senha, a confirmação por senha é recusada com a orientação certa
        excluirComSenha(aluno, "qualquerCoisa1").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("link")));

        pedirLink(aluno).andExpect(status().isAccepted());
        String codigo = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.EXCLUIR_CONTA, "it.ex.google@teste.com");
        assertTrue(existe(aluno.getId()), "pedir o link ainda não exclui nada");

        MockHttpServletResponse resposta = abrirLink(codigo).andExpect(status().isNoContent()).andReturn().getResponse();
        assertFalse(existe(aluno.getId()));
        assertTrue(apagouCookieDeLogin(resposta));
        assertEquals(1, ContasDeTeste.pedidos(eventos, TipoEmailConta.CONTA_EXCLUIDA, "it.ex.google@teste.com").size());

        abrirLink(codigo).andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("LINK_INVALIDO"));
    }

    @Test
    void contaComSenhaNaoPedeLinkEOLinkDeOutraFinalidadeNaoServe() throws Exception {
        Aluno aluno = aluno("it.ex.comsenha", true);
        mockMvc.perform(get("/api/auth/me").with(como(aluno))).andExpect(jsonPath("$.temSenha").value(true));
        pedirLink(aluno).andExpect(status().isBadRequest());

        // um link de "nova senha" não pode ser usado para excluir a conta
        String codigoDeSenha = tokenUsuarioService.gerar(aluno, br.com.logicup.backend.model.FinalidadeToken.REDEFINIR_SENHA);
        abrirLink(codigoDeSenha).andExpect(status().isBadRequest());
        assertTrue(existe(aluno.getId()));
    }

    @Test
    void linkVencidoNaoExclui() throws Exception {
        Aluno aluno = aluno("it.ex.vencido", false);
        pedirLink(aluno).andExpect(status().isAccepted());
        String codigo = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.EXCLUIR_CONTA, "it.ex.vencido@teste.com");
        jdbc.update("UPDATE token_usuario SET expira_em = NOW() - INTERVAL 1 MINUTE WHERE hash_token = ?",
                TokenUsuarioService.hash(codigo));
        entityManager.clear();

        abrirLink(codigo).andExpect(status().isBadRequest());
        assertTrue(existe(aluno.getId()));
    }
}
