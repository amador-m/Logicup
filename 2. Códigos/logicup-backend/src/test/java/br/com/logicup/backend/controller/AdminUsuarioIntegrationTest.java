package br.com.logicup.backend.controller;

import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import br.com.logicup.backend.service.AceiteService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 11: GESTÃO DE USUÁRIOS pelo admin (listar, buscar, bloquear e desbloquear).
 * O teste mais importante é o do bloqueio: a conta bloqueada não entra pelo login E quem já estava logado
 * (com o cookie de verdade) perde o acesso na hora; ao desbloquear, tudo volta, sem perder dados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUsuarioIntegrationTest {

    private static final String SENHA = "senhaForte123";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private TokenService tokenService;
    @Autowired private AceiteService aceiteService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;

    private Usuario admin;
    private Aluno aluno;

    @BeforeEach
    void preparar() {
        admin = usuarioRepository.save(ContasDeTeste.confirmada(new Usuario("Admin Teste", "it.adm.admin",
                "it.adm.admin@teste.com", passwordEncoder.encode(SENHA), TipoUsuario.ADMIN)));
        aluno = alunoRepository.save(ContasDeTeste.confirmada(new Aluno("Bruna Bloqueio", "it.adm.bruna",
                "it.adm.bruna@teste.com", passwordEncoder.encode(SENHA))));
        aceiteService.registrarVersoesVigentes(aluno);
    }

    private static RequestPostProcessor como(Usuario usuario) {
        String papel = usuario.getTipoUsuario().name();
        return jwt().jwt(t -> t.subject(String.valueOf(usuario.getId())).claim("role", papel)
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_" + papel));
    }

    private ResultActions bloqueio(Usuario quem, Integer alvo, boolean bloquear) throws Exception {
        return mockMvc.perform(put("/api/admin/usuarios/{id}/bloqueio", alvo).with(como(quem)).with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content("{\"bloqueado\": " + bloquear + "}"));
    }

    private ResultActions login(String quem) throws Exception {
        return mockMvc.perform(post("/api/auth/login").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\": \"" + quem + "\", \"senha\": \"" + SENHA + "\"}"));
    }

    // ---------------- listar e buscar ----------------

    @Test
    void adminBuscaPorParteDoEmailENaoVeSenha() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios").param("busca", "IT.ADM.BRU").with(como(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].username").value("it.adm.bruna"))
                .andExpect(jsonPath("$.itens[0].perfil").value("ALUNO"))
                .andExpect(jsonPath("$.itens[0].bloqueado").value(false))
                .andExpect(jsonPath("$.itens[0].temSenha").value(true))
                .andExpect(jsonPath("$.itens[0].senha").doesNotExist());
    }

    @Test
    void filtroPorPerfilEPaginacao() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios").param("busca", "it.adm.").param("perfil", "ADMIN")
                        .with(como(admin)))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].perfil").value("ADMIN"))
                .andExpect(jsonPath("$.pagina").value(0));
        mockMvc.perform(get("/api/admin/usuarios").param("perfil", "NAO_EXISTE").with(como(admin)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/admin/usuarios").param("pagina", "-1").with(como(admin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void caracteresEspeciaisDaBuscaSaoLiteraisENaoCuringas() throws Exception {
        // "%" sem escape casaria com TODO mundo; escapado, procura um "%" de verdade (ninguém tem)
        mockMvc.perform(get("/api/admin/usuarios").param("busca", "%").with(como(admin)))
                .andExpect(jsonPath("$.total").value(0));
        // "_" sem escape casaria com qualquer letra; aqui só acha quem tem "_" no nome/usuário/e-mail
        mockMvc.perform(get("/api/admin/usuarios").param("busca", "it_adm").with(como(admin)))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void soAdminEntraNaGestaoDeUsuarios() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios").with(como(aluno))).andExpect(status().isForbidden());
        Usuario professor = usuarioRepository.save(ContasDeTeste.confirmada(new Usuario("Prof", "it.adm.prof",
                "it.adm.prof@teste.com", "hash", TipoUsuario.PROFESSOR)));
        mockMvc.perform(get("/api/admin/usuarios").with(como(professor))).andExpect(status().isForbidden());
        bloqueio(professor, aluno.getId(), true).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/usuarios")).andExpect(status().isUnauthorized());
    }

    // ---------------- bloquear e desbloquear ----------------

    @Test
    void contaBloqueadaNaoEntraEPerdeOAcessoNaHoraMasNaoPerdeNada() throws Exception {
        jdbc.update("UPDATE aluno SET pontuacao_acumulada = 120 WHERE id_usuario = ?", aluno.getId());
        // a aluna está logada (cookie de verdade, como no navegador)
        Cookie sessao = new Cookie(TokenService.NOME_COOKIE, tokenService.gerarToken(aluno, true));
        mockMvc.perform(get("/api/auth/me").cookie(sessao)).andExpect(status().isOk());

        bloqueio(admin, aluno.getId(), true)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bloqueado").value(true))
                .andExpect(jsonPath("$.bloqueadoEm").isNotEmpty());
        entityManager.flush();

        // a sessão que já existia é cortada na hora (o token ainda é válido, mas o filtro consulta o banco)
        mockMvc.perform(get("/api/auth/me").cookie(sessao))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CONTA_BLOQUEADA"));
        mockMvc.perform(get("/api/trilha").cookie(sessao)).andExpect(status().isUnauthorized());
        // sair continua funcionando
        mockMvc.perform(post("/api/auth/logout").cookie(sessao).with(csrfValido())).andExpect(status().isNoContent());

        // e o login com a senha CERTA é recusado com a mensagem de conta bloqueada
        login("it.adm.bruna").andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("CONTA_BLOQUEADA"));

        // desbloqueia: entra de novo e o XP continua lá
        bloqueio(admin, aluno.getId(), false).andExpect(jsonPath("$.bloqueado").value(false));
        entityManager.flush();
        login("it.adm.bruna").andExpect(status().isOk());
        assertEquals(120, jdbc.queryForObject("SELECT pontuacao_acumulada FROM aluno WHERE id_usuario = ?",
                Integer.class, aluno.getId()));
    }

    @Test
    void senhaErradaEmContaBloqueadaContinuaComOErroDeSempre() throws Exception {
        // quem está chutando senha não descobre que a conta existe nem que está bloqueada
        bloqueio(admin, aluno.getId(), true).andExpect(status().isOk());
        entityManager.flush();
        mockMvc.perform(post("/api/auth/login").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\": \"it.adm.bruna\", \"senha\": \"senhaErrada99\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").doesNotExist());
    }

    @Test
    void adminNaoBloqueiaASiMesmoNemOutroAdmin() throws Exception {
        bloqueio(admin, admin.getId(), true).andExpect(status().isBadRequest());
        Usuario outroAdmin = usuarioRepository.save(ContasDeTeste.confirmada(new Usuario("Outro", "it.adm.outro",
                "it.adm.outro@teste.com", "hash", TipoUsuario.ADMIN)));
        bloqueio(admin, outroAdmin.getId(), true).andExpect(status().isBadRequest());
        bloqueio(admin, 999_999_999, true).andExpect(status().isNotFound());
    }

    @Test
    void bloquearDuasVezesNaoMudaADataOriginal() throws Exception {
        String primeira = bloqueio(admin, aluno.getId(), true).andReturn().getResponse().getContentAsString();
        String segunda = bloqueio(admin, aluno.getId(), true).andReturn().getResponse().getContentAsString();
        assertEquals(primeira, segunda);
        mockMvc.perform(get("/api/admin/usuarios").param("busca", "it.adm.bruna").with(como(admin)))
                .andExpect(jsonPath("$.itens[*].bloqueado", hasItem(true)))
                .andExpect(jsonPath("$.itens[*].bloqueado", not(hasItem(false))));
    }
}
