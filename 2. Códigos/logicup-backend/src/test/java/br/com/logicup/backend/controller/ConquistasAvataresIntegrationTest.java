package br.com.logicup.backend.controller;

import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.Modulo;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.TipoExercicio;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ModuloRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 9 — testes de INTEGRAÇÃO de conquistas, gemas e avatares (decisões C1–C3, M1–M5, B1).
 *
 * Trilha própria de teste, com fases de ordenação NEGATIVA (vêm antes da Fase 1 real do seed):
 *   Fase A (-200): rodada A1 (1 questão comum, 10 XP) e rodada A2 (1 questão BOSS, 10 XP)
 *   Fase B (-100): rodada B1 (1 questão)
 * Aprovar A1 e A2 conclui a Fase A inteira.
 * O que a API não mostra (linhas gravadas, saldo direto na tabela) é conferido com JdbcTemplate.
 * @Transactional: tudo é desfeito no fim de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ConquistasAvataresIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @PersistenceContext private EntityManager em;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private TokenService tokenService;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String CONTEUDO = """
            {"objetivos":["Objetivo"],"teoria":[{"titulo":"T","texto":"Texto"}],"exemplos":[]}""";
    private static final String CERTA = "{\"alternativa\":\"a\"}";
    private static final String ERRADA = "{\"alternativa\":\"b\"}";
    private static final String SENHA = "SenhaForte123";

    private Usuario professor;
    private Aluno ana;
    private Rodada rodadaA1;
    private Rodada rodadaA2;
    private Exercicio questaoA1;
    private Exercicio bossA2;

    @BeforeEach
    void montar() {
        Modulo faseA = moduloRepository.save(new Modulo("Fase de teste A", "Teste", -200));
        Modulo faseB = moduloRepository.save(new Modulo("Fase de teste B", "Teste", -100));
        rodadaA1 = rodadaRepository.save(new Rodada(faseA, "Rodada A1", 1, CONTEUDO));
        rodadaA2 = rodadaRepository.save(new Rodada(faseA, "Rodada A2", 2, CONTEUDO));
        Rodada rodadaB1 = rodadaRepository.save(new Rodada(faseB, "Rodada B1", 1, CONTEUDO));
        questaoA1 = exercicio(rodadaA1, "IT.C.A1", TipoExercicio.MULTIPLA_ESCOLHA);
        bossA2 = exercicio(rodadaA2, "IT.C.A2", TipoExercicio.BOSS_CHALLENGE);
        exercicio(rodadaB1, "IT.C.B1", TipoExercicio.MULTIPLA_ESCOLHA);

        professor = usuarioRepository.save(ContasDeTeste.confirmada(new Usuario("Professora Conquista", "prof.conq.it",
                "prof.conq.it@teste.com", passwordEncoder.encode(SENHA), TipoUsuario.PROFESSOR)));
        ana = alunoRepository.save(new Aluno("Ana Conquista", "ana.conq.it", "ana.conq.it@teste.com",
                passwordEncoder.encode(SENHA)));
    }

    private Exercicio exercicio(Rodada rodada, String codigo, TipoExercicio tipo) {
        Exercicio exercicio = new Exercicio(rodada, codigo, "Questão " + codigo, 1, tipo,
                FormatoResposta.ESCOLHA_UNICA, 10, "Enunciado", "{\"correta\":\"a\"}", "Explicação");
        exercicio.setAlternativasJson("[{\"id\":\"a\",\"texto\":\"Sim\"},{\"id\":\"b\",\"texto\":\"Não\"}]");
        return exercicioRepository.save(exercicio);
    }

    // ---------- helpers ----------

    /** flush() antes de ler com JDBC: o teste não faz commit e o Hibernate pode estar segurando gravações. */
    private JdbcTemplate banco() {
        em.flush();
        return jdbc;
    }

    private static RequestPostProcessor logado(Integer id, String role) {
        return jwt().jwt(token -> token.subject(String.valueOf(id)).claim("role", role)
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private RequestPostProcessor como(Aluno aluno) { return logado(aluno.getId(), "ALUNO"); }
    private RequestPostProcessor comoProfessor() { return logado(professor.getId(), "PROFESSOR"); }

    private ResultActions enviarJson(MockHttpServletRequestBuilder req, RequestPostProcessor usuario, String json)
            throws Exception {
        return mockMvc.perform(req.with(usuario).with(csrfValido()).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions semCorpo(MockHttpServletRequestBuilder req, RequestPostProcessor usuario) throws Exception {
        return mockMvc.perform(req.with(usuario).with(csrfValido()));
    }

    private void responder(Aluno aluno, Rodada rodada, Exercicio exercicio, String resposta) throws Exception {
        enviarJson(post("/api/rodadas/{id}/respostas", rodada.getId()), como(aluno),
                "{\"respostas\":[{\"exercicioId\":" + exercicio.getId() + ",\"resposta\":" + resposta + "}]}")
                .andExpect(status().isOk());
    }

    private int gemasNoBanco(Aluno aluno) {
        return banco().queryForObject("SELECT gemas FROM aluno WHERE id_usuario = ?", Integer.class, aluno.getId());
    }

    private void darGemas(Aluno aluno, int gemas) {
        banco().update("UPDATE aluno SET gemas = ? WHERE id_usuario = ?", gemas, aluno.getId());
    }

    private List<String> medalhasNoBanco(Aluno aluno) {
        return banco().queryForList("SELECT CONCAT(codigo_conquista, ':', nivel) FROM aluno_conquista "
                + "WHERE id_usuario = ? ORDER BY codigo_conquista, nivel", String.class, aluno.getId());
    }

    /** Valor atual de uma conquista no GET /api/conquistas (ex.: quantos acertos de primeira). */
    private int valorAtual(Aluno aluno, String codigo) throws Exception {
        String corpo = mockMvc.perform(get("/api/conquistas").with(como(aluno)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<Integer> valores = JsonPath.read(corpo, "$.conquistas[?(@.codigo == '" + codigo + "')].valorAtual");
        return valores.get(0);
    }

    // =====================================================================
    // Permissões
    // =====================================================================

    @Test
    void semLoginRecebe401() throws Exception {
        mockMvc.perform(get("/api/conquistas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/avatares")).andExpect(status().isUnauthorized());
    }

    @Test
    void conquistasECompraSaoSoDoAluno() throws Exception {
        mockMvc.perform(get("/api/conquistas").with(comoProfessor())).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/conquistas/novas").with(comoProfessor())).andExpect(status().isForbidden());
        semCorpo(post("/api/conquistas/vistas"), comoProfessor()).andExpect(status().isForbidden());
        semCorpo(post("/api/avatares/CACHORRO_CLARO/compra"), comoProfessor()).andExpect(status().isForbidden());
    }

    // =====================================================================
    // Conquistas (C1, C2, M3, M4, M5, B1)
    // =====================================================================

    @Test
    void alunoNovoVeOCatalogoInteiroSemNadaDesbloqueado() throws Exception {
        mockMvc.perform(get("/api/conquistas").with(como(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gemas").value(0))
                .andExpect(jsonPath("$.conquistas", hasSize(8)))
                .andExpect(jsonPath("$.conquistas[0].codigo").value("PRIMEIRO_ACERTO"))
                .andExpect(jsonPath("$.conquistas[0].niveis[0].nivel").value("UNICA"))
                .andExpect(jsonPath("$.conquistas[1].codigo").value("RODADAS"))
                .andExpect(jsonPath("$.conquistas[1].niveis[*].meta").value(org.hamcrest.Matchers.contains(1, 5, 15)))
                .andExpect(jsonPath("$.conquistas[1].niveis[*].gemas").value(org.hamcrest.Matchers.contains(15, 30, 60)))
                .andExpect(jsonPath("$.conquistas[1].niveis[1].descricao").value("Seja aprovado em 5 rodada(s)."))
                .andExpect(jsonPath("$.conquistas[*].niveis[*].desbloqueadaEm", everyItem(nullValue())));
        mockMvc.perform(get("/api/conquistas/novas").with(como(ana)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void acertarDePrimeiraRendeMedalhasEGemasNaMesmaTransacaoDoEnvio() throws Exception {
        responder(ana, rodadaA1, questaoA1, CERTA);

        // aprovou a A1: "Primeiro acerto" (única, 15) + "Rodada após rodada" bronze (15) = 30 gemas
        assertEquals(List.of("PRIMEIRO_ACERTO:UNICA", "RODADAS:BRONZE"), medalhasNoBanco(ana));
        assertEquals(30, gemasNoBanco(ana));

        mockMvc.perform(get("/api/conquistas/novas").with(como(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].codigo").value("PRIMEIRO_ACERTO"))
                .andExpect(jsonPath("$[0].titulo").value("Primeiro acerto"))
                .andExpect(jsonPath("$[0].gemas").value(15))
                .andExpect(jsonPath("$[1].codigo").value("RODADAS"))
                .andExpect(jsonPath("$[1].nivel").value("BRONZE"))
                .andExpect(jsonPath("$[1].descricao").value("Seja aprovado em 1 rodada(s)."));
        mockMvc.perform(get("/api/conquistas").with(como(ana)))
                .andExpect(jsonPath("$.gemas").value(30))
                .andExpect(jsonPath("$.conquistas[0].niveis[0].desbloqueadaEm", notNullValue()))
                .andExpect(jsonPath("$.conquistas[1].valorAtual").value(1));
        assertEquals(1, valorAtual(ana, "ACERTO_DE_PRIMEIRA"));
    }

    @Test
    void refazerARodadaNaoPagaAsMesmasMedalhasDeNovo() throws Exception {
        responder(ana, rodadaA1, questaoA1, CERTA);
        responder(ana, rodadaA1, questaoA1, CERTA);
        responder(ana, rodadaA1, questaoA1, ERRADA);

        assertEquals(List.of("PRIMEIRO_ACERTO:UNICA", "RODADAS:BRONZE"), medalhasNoBanco(ana));
        assertEquals(30, gemasNoBanco(ana));
        // refazer (mesmo errando depois) não tira o "acerto de primeira": o XP gravado não muda
        assertEquals(1, valorAtual(ana, "ACERTO_DE_PRIMEIRA"));
    }

    @Test
    void quemErrouAntesDeAcertarNaoContaComoAcertoDePrimeira() throws Exception {
        responder(ana, rodadaA1, questaoA1, ERRADA);
        assertEquals(List.of(), medalhasNoBanco(ana)); // errar não dá medalha nenhuma
        assertEquals(0, gemasNoBanco(ana));

        responder(ana, rodadaA1, questaoA1, CERTA);
        assertEquals(List.of("PRIMEIRO_ACERTO:UNICA", "RODADAS:BRONZE"), medalhasNoBanco(ana));
        assertEquals(0, valorAtual(ana, "ACERTO_DE_PRIMEIRA")); // PR7: só ganhou metade do XP
    }

    @Test
    void vencerOBossConcluiAFaseEDaAsMedalhasDeBossEFase() throws Exception {
        responder(ana, rodadaA1, questaoA1, CERTA);
        responder(ana, rodadaA2, bossA2, CERTA);

        assertEquals(List.of("BOSS:BRONZE", "FASES:BRONZE", "PRIMEIRO_ACERTO:UNICA", "RODADAS:BRONZE"),
                medalhasNoBanco(ana));
        assertEquals(60, gemasNoBanco(ana));
        assertEquals(2, valorAtual(ana, "RODADAS"));
        assertEquals(2, valorAtual(ana, "ACERTO_DE_PRIMEIRA"));
        assertEquals(1, valorAtual(ana, "FASES"));
    }

    @Test
    void depoisDeVistasAsNovasSomemESoAsSeguintesAparecem() throws Exception {
        responder(ana, rodadaA1, questaoA1, CERTA);

        semCorpo(post("/api/conquistas/vistas"), como(ana)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/conquistas/novas").with(como(ana))).andExpect(jsonPath("$", hasSize(0)));
        assertEquals(0, banco().queryForObject(
                "SELECT COUNT(*) FROM aluno_conquista WHERE id_usuario = ? AND vista_em IS NULL", Integer.class, ana.getId()));

        responder(ana, rodadaA2, bossA2, CERTA);
        mockMvc.perform(get("/api/conquistas/novas").with(como(ana)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].codigo").value("BOSS"))
                .andExpect(jsonPath("$[1].codigo").value("FASES"));
    }

    @Test
    void entrarEmUmaTurmaDaAConquistaColegaDeTurma() throws Exception {
        String corpo = enviarJson(post("/api/turmas"), comoProfessor(), "{\"nome\":\"Turma das conquistas\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String codigo = JsonPath.read(corpo, "$.codigo");

        enviarJson(post("/api/minha-turma"), como(ana), "{\"codigo\":\"" + codigo + "\"}").andExpect(status().isOk());

        assertEquals(List.of("TURMA:UNICA"), medalhasNoBanco(ana));
        assertEquals(15, gemasNoBanco(ana));
        mockMvc.perform(get("/api/conquistas/novas").with(como(ana)))
                .andExpect(jsonPath("$[0].codigo").value("TURMA"))
                .andExpect(jsonPath("$[0].titulo").value("Colega de turma"));
    }

    @Test
    void loginDaAsConquistasRetroativasDeQuemJaTinhaProgresso() throws Exception {
        // aluno "antigo": já tinha 150 XP e 3 dias de ofensiva antes desta versão (sem nenhuma medalha gravada)
        Aluno veterano = new Aluno("Veterano", "veterano.it", "veterano.it@teste.com", passwordEncoder.encode(SENHA));
        ContasDeTeste.confirmada(veterano);
        veterano.adicionarPontos(150);
        LocalDate dia = LocalDate.of(2026, 10, 1);
        veterano.registrarAtividade(dia);
        veterano.registrarAtividade(dia.plusDays(1));
        veterano.registrarAtividade(dia.plusDays(2));
        veterano = alunoRepository.save(veterano);
        assertEquals(List.of(), medalhasNoBanco(veterano));

        mockMvc.perform(post("/api/auth/login").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"veterano.it\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatar").value("GATO_CLARO"));

        assertEquals(List.of("OFENSIVA:BRONZE", "XP:BRONZE"), medalhasNoBanco(veterano));
        assertEquals(30, gemasNoBanco(veterano));

        // logar de novo não paga de novo (idempotente)
        mockMvc.perform(post("/api/auth/login").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"veterano.it\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk());
        assertEquals(30, gemasNoBanco(veterano));
    }

    @Test
    void loginDeProfessorContinuaFuncionando() throws Exception {
        mockMvc.perform(post("/api/auth/login").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"prof.conq.it\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("PROFESSOR"));
    }

    @Test
    void conquistaQueSaiuDoCatalogoNaoQuebraAsTelas() throws Exception {
        banco().update("INSERT INTO aluno_conquista (id_usuario, codigo_conquista, nivel, gemas_recebidas, desbloqueada_em) "
                + "VALUES (?, 'CONQUISTA_ANTIGA', 'BRONZE', 15, NOW())", ana.getId());

        mockMvc.perform(get("/api/conquistas/novas").with(como(ana)))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/api/conquistas").with(como(ana)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.conquistas", hasSize(8)));
    }

    // =====================================================================
    // Avatares e loja (opção 3: 2 gratuitos, o resto com gemas)
    // =====================================================================

    @Test
    void catalogoDoAlunoMostraSaldoOQuePossuiEOQueEstaEmUso() throws Exception {
        mockMvc.perform(get("/api/avatares").with(como(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gemas").value(0))
                .andExpect(jsonPath("$.avatarAtual").value("GATO_CLARO"))
                .andExpect(jsonPath("$.avatares", hasSize(8)))
                .andExpect(jsonPath("$.avatares[0].codigo").value("GATO_CLARO"))
                .andExpect(jsonPath("$.avatares[0].possui").value(true))
                .andExpect(jsonPath("$.avatares[0].emUso").value(true))
                .andExpect(jsonPath("$.avatares[1].codigo").value("GATO_ESCURO"))
                .andExpect(jsonPath("$.avatares[1].gratuito").value(true))
                .andExpect(jsonPath("$.avatares[1].possui").value(true))
                .andExpect(jsonPath("$.avatares[2].codigo").value("CACHORRO_CLARO"))
                .andExpect(jsonPath("$.avatares[2].preco").value(60))
                .andExpect(jsonPath("$.avatares[2].possui").value(false));
    }

    @Test
    void professorVeOCatalogoSemGemas() throws Exception {
        mockMvc.perform(get("/api/avatares").with(comoProfessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gemas").value(nullValue()))
                .andExpect(jsonPath("$.avatares[2].possui").value(false));
    }

    @Test
    void semGemasSuficientesACompraERecusadaENadaMuda() throws Exception {
        darGemas(ana, 59);
        semCorpo(post("/api/avatares/CACHORRO_CLARO/compra"), como(ana))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("60 gemas")));
        assertEquals(59, gemasNoBanco(ana));
        assertEquals(0, banco().queryForObject("SELECT COUNT(*) FROM aluno_avatar WHERE id_usuario = ?",
                Integer.class, ana.getId()));
    }

    @Test
    void comprarDebitaOPrecoEGuardaOPrecoPago() throws Exception {
        darGemas(ana, 70);
        semCorpo(post("/api/avatares/CACHORRO_CLARO/compra"), como(ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gemas").value(10))
                .andExpect(jsonPath("$.avatares[2].possui").value(true))
                .andExpect(jsonPath("$.avatares[2].emUso").value(false)); // comprar não troca o avatar sozinho

        assertEquals(10, gemasNoBanco(ana));
        assertEquals(60, banco().queryForObject(
                "SELECT preco_pago FROM aluno_avatar WHERE id_usuario = ? AND codigo_avatar = 'CACHORRO_CLARO'",
                Integer.class, ana.getId()));
    }

    @Test
    void naoDaParaComprarDuasVezesNemOGratuitoNemOInexistente() throws Exception {
        darGemas(ana, 200);
        semCorpo(post("/api/avatares/CACHORRO_CLARO/compra"), como(ana)).andExpect(status().isOk());
        semCorpo(post("/api/avatares/CACHORRO_CLARO/compra"), como(ana)).andExpect(status().isConflict());
        semCorpo(post("/api/avatares/GATO_ESCURO/compra"), como(ana)).andExpect(status().isConflict());
        semCorpo(post("/api/avatares/DRAGAO/compra"), como(ana)).andExpect(status().isNotFound());
        assertEquals(140, gemasNoBanco(ana)); // só a primeira compra debitou
    }

    @Test
    void comprarSemTokenCsrfERecusado() throws Exception {
        // login de VERDADE (cookie com JWT assinado): o jwt() do spring-security-test desliga o CSRF
        darGemas(ana, 100);
        Cookie login = new Cookie(TokenService.NOME_COOKIE, tokenService.gerarToken(ana, true));

        mockMvc.perform(post("/api/avatares/CACHORRO_CLARO/compra").cookie(login)).andExpect(status().isForbidden());
        assertEquals(100, gemasNoBanco(ana));

        mockMvc.perform(post("/api/avatares/CACHORRO_CLARO/compra").cookie(login).with(csrfValido()))
                .andExpect(status().isOk());
        assertEquals(40, gemasNoBanco(ana));
    }

    @Test
    void trocarParaUmGratuitoAtualizaOPerfil() throws Exception {
        enviarJson(put("/api/perfil/avatar"), como(ana), "{\"codigo\":\"GATO_ESCURO\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatar").value("GATO_ESCURO"));
        mockMvc.perform(get("/api/auth/me").with(como(ana))).andExpect(jsonPath("$.avatar").value("GATO_ESCURO"));
        assertEquals("GATO_ESCURO", banco().queryForObject("SELECT avatar FROM usuario WHERE id_usuario = ?",
                String.class, ana.getId()));
    }

    @Test
    void soDaParaUsarOAvatarDepoisDeComprar() throws Exception {
        enviarJson(put("/api/perfil/avatar"), como(ana), "{\"codigo\":\"CACHORRO_CLARO\"}")
                .andExpect(status().isForbidden());

        darGemas(ana, 60);
        semCorpo(post("/api/avatares/CACHORRO_CLARO/compra"), como(ana)).andExpect(status().isOk());
        enviarJson(put("/api/perfil/avatar"), como(ana), "{\"codigo\":\"CACHORRO_CLARO\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatar").value("CACHORRO_CLARO"));
    }

    @Test
    void codigoDeAvatarInvalidoOuVazioE400() throws Exception {
        enviarJson(put("/api/perfil/avatar"), como(ana), "{\"codigo\":\"DRAGAO\"}").andExpect(status().isBadRequest());
        enviarJson(put("/api/perfil/avatar"), como(ana), "{\"codigo\":\"  \"}").andExpect(status().isBadRequest());
    }

    @Test
    void professorUsaSoOsGratuitos() throws Exception {
        enviarJson(put("/api/perfil/avatar"), comoProfessor(), "{\"codigo\":\"GATO_ESCURO\"}")
                .andExpect(status().isOk()).andExpect(jsonPath("$.avatar").value("GATO_ESCURO"));
        enviarJson(put("/api/perfil/avatar"), comoProfessor(), "{\"codigo\":\"BARISTA_CLARO\"}")
                .andExpect(status().isForbidden());
    }

    // =====================================================================
    // O avatar aparece nos rankings e na turma (A2)
    // =====================================================================

    @Test
    void avatarApareceNoRankingGeralNoDaTurmaENaVisaoDoProfessor() throws Exception {
        responder(ana, rodadaA1, questaoA1, CERTA); // 10 XP: entra no ranking
        enviarJson(put("/api/perfil/avatar"), como(ana), "{\"codigo\":\"GATO_ESCURO\"}").andExpect(status().isOk());

        mockMvc.perform(get("/api/ranking").with(como(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.voce.avatar").value("GATO_ESCURO"));

        String corpo = enviarJson(post("/api/turmas"), comoProfessor(), "{\"nome\":\"Turma do avatar\"}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        Integer turmaId = JsonPath.read(corpo, "$.id");
        enviarJson(post("/api/minha-turma"), como(ana), "{\"codigo\":\"" + JsonPath.read(corpo, "$.codigo") + "\"}")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/minha-turma/ranking").with(como(ana)))
                .andExpect(jsonPath("$.posicoes[0].avatar").value("GATO_ESCURO"));
        mockMvc.perform(get("/api/turmas/{id}", turmaId).with(comoProfessor()))
                .andExpect(jsonPath("$.alunos[0].avatar").value("GATO_ESCURO"));
    }

    @Test
    void cadastroNovoComecaComOAvatarPadraoEZeroGemas() throws Exception {
        Aluno novo = alunoRepository.save(new Aluno("Novo", "novo.conq.it", "novo.conq.it@teste.com", "hash"));
        assertEquals("GATO_CLARO", banco().queryForObject("SELECT avatar FROM usuario WHERE id_usuario = ?",
                String.class, novo.getId()));
        assertEquals(0, gemasNoBanco(novo));
        assertNotNull(novo.getAvatar());
    }
}
