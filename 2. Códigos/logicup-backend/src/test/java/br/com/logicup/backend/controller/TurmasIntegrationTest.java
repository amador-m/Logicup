package br.com.logicup.backend.controller;

import br.com.logicup.backend.config.ClockConfig;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 9 — testes de INTEGRAÇÃO de professor e turmas, escritos ANTES da implementação.
 * Cada teste cita a regra do documento "LogiCup — Modelagem de professor e turmas" (R1 a R14).
 *
 * Só usa classes que JÁ existem: turmas, matrículas e tarefas são criadas pela própria API,
 * e o que a API não mostra (status da matrícula, linhas apagadas) é conferido no banco com JdbcTemplate.
 *
 * Trilha própria de teste com fases de ordenação NEGATIVA (vêm antes da Fase 1 real do seed):
 *   Fase A (-200): rodada A1 (1 questão, 10 XP) e rodada A2 (1 questão)   |   Fase B (-100): rodada B1
 * Para um aluno novo: A1 liberada; A2 e B1 bloqueadas. Acertar a questão da A1 aprova a A1.
 * @Transactional: tudo é desfeito no fim de cada teste.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TurmasIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @PersistenceContext private EntityManager em;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private TokenService tokenService;

    private static final String CONTEUDO = """
            {"objetivos":["Objetivo"],"teoria":[{"titulo":"T","texto":"Texto"}],"exemplos":[]}""";
    private static final String CERTA = "{\"alternativa\":\"a\"}";
    /** 8 caracteres do alfabeto sem 0/O, 1/I/L (R3). */
    private static final String FORMATO_CODIGO = "^[ABCDEFGHJKMNPQRSTUVWXYZ23456789]{8}$";

    private Usuario professor;
    private Usuario outroProfessor;
    private Aluno ana;
    private Aluno bruno;
    private Rodada rodadaA1;
    private Rodada rodadaA2;
    private Exercicio questaoA1;

    @BeforeEach
    void montar() {
        Modulo faseA = moduloRepository.save(new Modulo("Fase de teste A", "Teste", -200));
        Modulo faseB = moduloRepository.save(new Modulo("Fase de teste B", "Teste", -100));
        rodadaA1 = rodadaRepository.save(new Rodada(faseA, "Rodada A1", 1, CONTEUDO));
        rodadaA2 = rodadaRepository.save(new Rodada(faseA, "Rodada A2", 2, CONTEUDO));
        Rodada rodadaB1 = rodadaRepository.save(new Rodada(faseB, "Rodada B1", 1, CONTEUDO));
        questaoA1 = exercicio(rodadaA1, "IT.T.A1");
        exercicio(rodadaA2, "IT.T.A2");
        exercicio(rodadaB1, "IT.T.B1");

        professor = usuarioRepository.save(new Usuario("Professora Turma", "prof.turma.it",
                "prof.turma.it@teste.com", "hash", TipoUsuario.PROFESSOR));
        outroProfessor = usuarioRepository.save(new Usuario("Outro Professor", "outro.prof.it",
                "outro.prof.it@teste.com", "hash", TipoUsuario.PROFESSOR));
        ana = alunoRepository.save(new Aluno("Ana Turma", "ana.turma.it", "ana.turma.it@teste.com", "hash"));
        bruno = alunoRepository.save(new Aluno("Bruno Turma", "bruno.turma.it", "bruno.turma.it@teste.com", "hash"));
    }

    private Exercicio exercicio(Rodada rodada, String codigo) {
        Exercicio exercicio = new Exercicio(rodada, codigo, "Questão " + codigo, 1, TipoExercicio.MULTIPLA_ESCOLHA,
                FormatoResposta.ESCOLHA_UNICA, 10, "Enunciado", "{\"correta\":\"a\"}", "Explicação");
        exercicio.setAlternativasJson("[{\"id\":\"a\",\"texto\":\"Sim\"},{\"id\":\"b\",\"texto\":\"Não\"}]");
        return exercicioRepository.save(exercicio);
    }

    // ---------- helpers ----------

    /**
     * Acesso direto ao banco para conferir o que a API não mostra.
     * flush() ANTES: como o teste é @Transactional, a requisição não faz commit, e o Hibernate pode estar
     * segurando INSERT/UPDATE na memória (ex.: matrícula com chave composta, mudança de status).
     * Sem o flush, o JdbcTemplate leria o banco "atrasado". Em produção cada requisição faz commit.
     */
    private JdbcTemplate banco() {
        em.flush();
        return jdbc;
    }

    private static RequestPostProcessor logado(Integer id, String role) {
        return jwt().jwt(token -> token.subject(String.valueOf(id)).claim("role", role)
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private RequestPostProcessor comoProfessor() { return logado(professor.getId(), "PROFESSOR"); }
    private RequestPostProcessor comoOutroProfessor() { return logado(outroProfessor.getId(), "PROFESSOR"); }
    private RequestPostProcessor como(Aluno aluno) { return logado(aluno.getId(), "ALUNO"); }

    private ResultActions enviarJson(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req,
                                     RequestPostProcessor usuario, String json) throws Exception {
        return mockMvc.perform(req.with(usuario).with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions semCorpo(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder req,
                                   RequestPostProcessor usuario) throws Exception {
        return mockMvc.perform(req.with(usuario).with(csrfValido()));
    }

    /** Cria uma turma pela API e devolve {id, codigo}. */
    private String[] criarTurma(RequestPostProcessor prof, String nome) throws Exception {
        String corpo = enviarJson(post("/api/turmas"), prof, "{\"nome\":\"" + nome + "\"}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return new String[]{String.valueOf((Integer) JsonPath.read(corpo, "$.id")), JsonPath.read(corpo, "$.codigo")};
    }

    private ResultActions entrar(Aluno aluno, String codigo) throws Exception {
        return enviarJson(post("/api/minha-turma"), como(aluno), "{\"codigo\":\"" + codigo + "\"}");
    }

    private String statusMatricula(Aluno aluno, String turmaId) {
        return banco().queryForObject("SELECT status FROM aluno_turma WHERE id_usuario = ? AND id_turma = ?",
                String.class, aluno.getId(), Integer.valueOf(turmaId));
    }

    private void aprovarA1(Aluno aluno) throws Exception {
        enviarJson(post("/api/rodadas/{id}/respostas", rodadaA1.getId()), como(aluno),
                "{\"respostas\":[{\"exercicioId\":" + questaoA1.getId() + ",\"resposta\":" + CERTA + "}]}")
                .andExpect(status().isOk());
    }

    private String criarTarefa(String turmaId, Rodada rodada, String prazoJson) throws Exception {
        String corpo = enviarJson(post("/api/turmas/{id}/tarefas", turmaId), comoProfessor(),
                "{\"rodadaId\":" + rodada.getId() + ",\"prazo\":" + prazoJson + "}")
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return String.valueOf((Integer) JsonPath.read(corpo, "$.id"));
    }

    // =====================================================================
    // R1 — quem pode o quê
    // =====================================================================

    @Test
    void semLoginRecebe401() throws Exception {
        mockMvc.perform(get("/api/turmas")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/minha-turma")).andExpect(status().isUnauthorized());
    }

    @Test
    void alunoNaoGerenciaTurmasEProfessorNaoEntraEmTurma() throws Exception {
        enviarJson(post("/api/turmas"), como(ana), "{\"nome\":\"Turma do aluno\"}").andExpect(status().isForbidden());
        mockMvc.perform(get("/api/turmas").with(como(ana))).andExpect(status().isForbidden());

        String[] turma = criarTurma(comoProfessor(), "Turma R1");
        enviarJson(post("/api/minha-turma"), comoProfessor(), "{\"codigo\":\"" + turma[1] + "\"}")
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/minha-turma").with(comoProfessor())).andExpect(status().isForbidden());
    }

    // =====================================================================
    // R2, R3 — criar, listar, renomear, código
    // =====================================================================

    @Test
    void professorCriaTurmaERecebeCodigoGeradoPeloServidor() throws Exception {
        enviarJson(post("/api/turmas"), comoProfessor(), "{\"nome\":\"  Lógica 2026  \",\"codigo\":\"AAAAAAAA\"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Lógica 2026"))          // espaços das pontas saem
                .andExpect(jsonPath("$.codigo").value(matchesPattern(FORMATO_CODIGO)))
                .andExpect(jsonPath("$.codigo").value(not("AAAAAAAA")))      // R3: o cliente não escolhe o código
                .andExpect(jsonPath("$.alunosAtivos").value(0));
    }

    @Test
    void nomeDeTurmaInvalidoRecebe400() throws Exception {
        for (String nome : new String[]{"", "   ", "ab", "x".repeat(101)}) {
            enviarJson(post("/api/turmas"), comoProfessor(), "{\"nome\":\"" + nome + "\"}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.campos.nome").exists());
        }
        Integer total = banco().queryForObject("SELECT COUNT(*) FROM turma WHERE id_professor = ?",
                Integer.class, professor.getId());
        assertEquals(0, total);
    }

    @Test
    void professorListaSoAsPropriasTurmas() throws Exception {
        criarTurma(comoProfessor(), "Turma Manhã");
        criarTurma(comoProfessor(), "Turma Noite");
        criarTurma(comoOutroProfessor(), "Turma de Outro");

        mockMvc.perform(get("/api/turmas").with(comoProfessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].nome", containsInAnyOrder("Turma Manhã", "Turma Noite")))
                .andExpect(content().string(not(containsString("Turma de Outro"))));
    }

    @Test
    void professorRenomeiaTurma() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Nome antigo");
        enviarJson(put("/api/turmas/{id}", turma[0]), comoProfessor(), "{\"nome\":\"Nome novo\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome novo"))
                .andExpect(jsonPath("$.codigo").value(turma[1]));
        enviarJson(put("/api/turmas/{id}", turma[0]), comoProfessor(), "{\"nome\":\"x\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void novoCodigoInvalidaOAntigo() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma R3");
        String corpo = semCorpo(post("/api/turmas/{id}/codigo", turma[0]), comoProfessor())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(matchesPattern(FORMATO_CODIGO)))
                .andReturn().getResponse().getContentAsString();
        String novo = JsonPath.read(corpo, "$.codigo");
        assertNotEquals(turma[1], novo);

        entrar(ana, turma[1]).andExpect(status().isNotFound());   // o antigo deixou de valer
        entrar(ana, novo).andExpect(status().isOk());
    }

    // =====================================================================
    // R8 — turma de outro professor = 404 (igual a inexistente)
    // =====================================================================

    @Test
    void turmaDeOutroProfessorResponde404EmTudo() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma protegida");
        entrar(ana, turma[1]).andExpect(status().isOk());
        String id = turma[0];

        mockMvc.perform(get("/api/turmas/{id}", id).with(comoOutroProfessor())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/turmas/{id}/ranking", id).with(comoOutroProfessor())).andExpect(status().isNotFound());
        enviarJson(put("/api/turmas/{id}", id), comoOutroProfessor(), "{\"nome\":\"Invadida\"}").andExpect(status().isNotFound());
        semCorpo(post("/api/turmas/{id}/codigo", id), comoOutroProfessor()).andExpect(status().isNotFound());
        semCorpo(delete("/api/turmas/{id}/alunos/{aluno}", id, ana.getId()), comoOutroProfessor()).andExpect(status().isNotFound());
        enviarJson(post("/api/turmas/{id}/tarefas", id), comoOutroProfessor(),
                "{\"rodadaId\":" + rodadaA1.getId() + ",\"prazo\":null}").andExpect(status().isNotFound());
        semCorpo(delete("/api/turmas/{id}", id), comoOutroProfessor()).andExpect(status().isNotFound());

        // nada mudou
        mockMvc.perform(get("/api/turmas/{id}", id).with(comoProfessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Turma protegida"))
                .andExpect(jsonPath("$.codigo").value(turma[1]))
                .andExpect(jsonPath("$.alunos.length()").value(1));
        mockMvc.perform(get("/api/turmas/{id}", Integer.MAX_VALUE).with(comoProfessor())).andExpect(status().isNotFound());
    }

    // =====================================================================
    // R4, R5, R6 — entrar e sair (lado do aluno)
    // =====================================================================

    @Test
    void alunoSemTurmaRecebe204() throws Exception {
        mockMvc.perform(get("/api/minha-turma").with(como(ana))).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/minha-turma/ranking").with(como(ana))).andExpect(status().isNotFound());
        semCorpo(delete("/api/minha-turma"), como(ana)).andExpect(status().isNotFound());
    }

    @Test
    void alunoEntraComCodigoNormalizado() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma R4");
        entrar(ana, "  " + turma[1].toLowerCase() + " ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Turma R4"))
                .andExpect(jsonPath("$.professor").value("Professora Turma"));
        assertEquals("ATIVO", statusMatricula(ana, turma[0]));

        mockMvc.perform(get("/api/minha-turma").with(como(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Turma R4"))
                .andExpect(jsonPath("$.tarefas.length()").value(0))
                .andExpect(content().string(not(containsString("prof.turma.it@teste.com")))); // o aluno vê só o NOME do professor
    }

    @Test
    void codigoInexistenteRecebe404() throws Exception {
        entrar(ana, "ZZZZZZZZ").andExpect(status().isNotFound());
        entrar(ana, "").andExpect(status().isBadRequest());
    }

    @Test
    void alunoComTurmaAtivaNaoEntraEmOutra() throws Exception {
        String[] t1 = criarTurma(comoProfessor(), "Turma 1");
        String[] t2 = criarTurma(comoOutroProfessor(), "Turma 2");
        entrar(ana, t1[1]).andExpect(status().isOk());

        entrar(ana, t2[1]).andExpect(status().isConflict());
        entrar(ana, t1[1]).andExpect(status().isConflict()); // nem na mesma de novo
        Integer emT2 = banco().queryForObject("SELECT COUNT(*) FROM aluno_turma WHERE id_usuario = ? AND id_turma = ?",
                Integer.class, ana.getId(), Integer.valueOf(t2[0]));
        assertEquals(0, emT2);
    }

    @Test
    void alunoSaiEPodeVoltarOuEntrarEmOutra() throws Exception {
        String[] t1 = criarTurma(comoProfessor(), "Turma 1");
        String[] t2 = criarTurma(comoOutroProfessor(), "Turma 2");
        entrar(ana, t1[1]).andExpect(status().isOk());

        semCorpo(delete("/api/minha-turma"), como(ana)).andExpect(status().isNoContent());
        assertEquals("SAIU", statusMatricula(ana, t1[0]));
        mockMvc.perform(get("/api/minha-turma").with(como(ana))).andExpect(status().isNoContent());

        // volta para a mesma: a MESMA linha volta para ATIVO (não duplica)
        entrar(ana, t1[1]).andExpect(status().isOk());
        assertEquals("ATIVO", statusMatricula(ana, t1[0]));
        Integer linhas = banco().queryForObject("SELECT COUNT(*) FROM aluno_turma WHERE id_usuario = ?",
                Integer.class, ana.getId());
        assertEquals(1, linhas);

        // sai de novo e entra em outra
        semCorpo(delete("/api/minha-turma"), como(ana)).andExpect(status().isNoContent());
        entrar(ana, t2[1]).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Turma 2"));
    }

    // =====================================================================
    // R7, R9 — acompanhamento e remoção
    // =====================================================================

    @Test
    void professorAcompanhaSoAlunosAtivosComProgressoEEmail() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma R9");
        entrar(ana, turma[1]).andExpect(status().isOk());
        entrar(bruno, turma[1]).andExpect(status().isOk());
        aprovarA1(ana);
        semCorpo(delete("/api/minha-turma"), como(bruno)).andExpect(status().isNoContent()); // Bruno saiu

        mockMvc.perform(get("/api/turmas/{id}", turma[0]).with(comoProfessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alunosAtivos").value(1))
                .andExpect(jsonPath("$.alunos.length()").value(1))
                .andExpect(jsonPath("$.alunos[0].id").value(ana.getId()))
                .andExpect(jsonPath("$.alunos[0].nome").value("Ana Turma"))
                .andExpect(jsonPath("$.alunos[0].username").value("ana.turma.it"))
                .andExpect(jsonPath("$.alunos[0].email").value("ana.turma.it@teste.com")) // decisão A3
                .andExpect(jsonPath("$.alunos[0].xp").value(10))
                .andExpect(jsonPath("$.alunos[0].nivel").isNumber())
                .andExpect(jsonPath("$.alunos[0].rodadasAprovadas").value(1))
                .andExpect(jsonPath("$.alunos[0].ultimaAtividade").exists())
                .andExpect(content().string(not(containsString("bruno.turma.it"))))
                .andExpect(content().string(not(containsString("senha"))))
                .andExpect(content().string(not(containsString("hash"))));

        mockMvc.perform(get("/api/turmas").with(comoProfessor()))
                .andExpect(jsonPath("$[0].alunosAtivos").value(1));
    }

    @Test
    void professorRemoveAlunoQueNaoVoltaPeloCodigo() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma R7");
        entrar(ana, turma[1]).andExpect(status().isOk());

        semCorpo(delete("/api/turmas/{id}/alunos/{aluno}", turma[0], ana.getId()), comoProfessor())
                .andExpect(status().isNoContent());
        assertEquals("REMOVIDO", statusMatricula(ana, turma[0]));

        mockMvc.perform(get("/api/minha-turma").with(como(ana))).andExpect(status().isNoContent());
        entrar(ana, turma[1]).andExpect(status().isForbidden());
        assertEquals("REMOVIDO", statusMatricula(ana, turma[0]));
        mockMvc.perform(get("/api/turmas/{id}", turma[0]).with(comoProfessor()))
                .andExpect(jsonPath("$.alunos.length()").value(0));

        // removida de uma turma, pode entrar em OUTRA
        String[] outra = criarTurma(comoOutroProfessor(), "Outra turma");
        entrar(ana, outra[1]).andExpect(status().isOk());
    }

    @Test
    void removerAlunoQueNaoEstaNaTurmaRecebe404() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma vazia");
        semCorpo(delete("/api/turmas/{id}/alunos/{aluno}", turma[0], ana.getId()), comoProfessor())
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // R14 — excluir turma
    // =====================================================================

    @Test
    void excluirTurmaApagaMatriculasETarefasMasNaoOProgresso() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma a excluir");
        entrar(ana, turma[1]).andExpect(status().isOk());
        criarTarefa(turma[0], rodadaA1, "null");
        aprovarA1(ana);
        Integer progressoAntes = banco().queryForObject("SELECT COUNT(*) FROM progresso_exercicio WHERE id_usuario = ?",
                Integer.class, ana.getId());

        semCorpo(delete("/api/turmas/{id}", turma[0]), comoProfessor()).andExpect(status().isNoContent());

        Integer turmaId = Integer.valueOf(turma[0]);
        assertEquals(0, banco().queryForObject("SELECT COUNT(*) FROM turma WHERE id_turma = ?", Integer.class, turmaId));
        assertEquals(0, banco().queryForObject("SELECT COUNT(*) FROM aluno_turma WHERE id_turma = ?", Integer.class, turmaId));
        assertEquals(0, banco().queryForObject("SELECT COUNT(*) FROM tarefa WHERE id_turma = ?", Integer.class, turmaId));
        assertEquals(progressoAntes, banco().queryForObject(
                "SELECT COUNT(*) FROM progresso_exercicio WHERE id_usuario = ?", Integer.class, ana.getId()));

        mockMvc.perform(get("/api/minha-turma").with(como(ana))).andExpect(status().isNoContent());
        entrar(ana, turma[1]).andExpect(status().isNotFound());
    }

    // =====================================================================
    // R10 — ranking da turma
    // =====================================================================

    @Test
    void rankingDaTurmaTemSoMembrosAtivosInclusiveComZeroXp() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma ranking");
        entrar(ana, turma[1]).andExpect(status().isOk());
        entrar(bruno, turma[1]).andExpect(status().isOk());
        aprovarA1(ana);

        mockMvc.perform(get("/api/turmas/{id}/ranking", turma[0]).with(comoProfessor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posicoes.length()").value(2))
                .andExpect(jsonPath("$.posicoes[0].username").value("ana.turma.it"))
                .andExpect(jsonPath("$.posicoes[0].posicao").value(1))
                .andExpect(jsonPath("$.posicoes[0].xp").value(10))
                .andExpect(jsonPath("$.posicoes[1].username").value("bruno.turma.it"))
                .andExpect(jsonPath("$.posicoes[1].xp").value(0))
                .andExpect(jsonPath("$.posicoes[1].posicao").value(2));

        // o aluno vê o mesmo ranking, com "voce" marcado e sem e-mail de ninguém
        mockMvc.perform(get("/api/minha-turma/ranking").with(como(bruno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posicoes.length()").value(2))
                .andExpect(jsonPath("$.posicoes[0].voce").value(false))
                .andExpect(jsonPath("$.posicoes[1].voce").value(true))
                .andExpect(content().string(not(containsString("@teste.com"))));

        semCorpo(delete("/api/minha-turma"), como(bruno)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/turmas/{id}/ranking", turma[0]).with(comoProfessor()))
                .andExpect(jsonPath("$.posicoes.length()").value(1));
    }

    // =====================================================================
    // R11, R12, R13 — tarefas
    // =====================================================================

    @Test
    void tarefasTemStatusPorAlunoEContagemParaOProfessor() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma tarefas");
        entrar(ana, turma[1]).andExpect(status().isOk());
        entrar(bruno, turma[1]).andExpect(status().isOk());
        String prazo = "\"" + LocalDate.now(ClockConfig.FUSO_LOGICUP).plusDays(7) + "\"";
        criarTarefa(turma[0], rodadaA1, prazo);
        criarTarefa(turma[0], rodadaA2, "null");

        // antes: A1 pendente (liberada), A2 bloqueada (R11)
        mockMvc.perform(get("/api/minha-turma").with(como(ana)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarefas.length()").value(2))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA1.getId() + ")].status").value("PENDENTE"))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA1.getId() + ")].rodada").value("Rodada A1"))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA1.getId() + ")].fase").value("Fase de teste A"))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA1.getId() + ")].atrasada").value(false))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA2.getId() + ")].status").value("BLOQUEADA"));

        aprovarA1(ana);

        mockMvc.perform(get("/api/minha-turma").with(como(ana)))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA1.getId() + ")].status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA2.getId() + ")].status").value("PENDENTE"));

        mockMvc.perform(get("/api/turmas/{id}", turma[0]).with(comoProfessor()))
                .andExpect(jsonPath("$.tarefas.length()").value(2))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA1.getId() + ")].concluiram").value(1))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA2.getId() + ")].concluiram").value(0))
                .andExpect(jsonPath("$.tarefas[?(@.rodadaId == " + rodadaA2.getId() + ")].prazo").value(contains(nullValue())));
    }

    @Test
    void tarefaInvalidaOuRepetidaERecusada() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma tarefas inválidas");
        criarTarefa(turma[0], rodadaA1, "null");

        enviarJson(post("/api/turmas/{id}/tarefas", turma[0]), comoProfessor(),
                "{\"rodadaId\":" + rodadaA1.getId() + ",\"prazo\":null}").andExpect(status().isConflict());   // R11
        enviarJson(post("/api/turmas/{id}/tarefas", turma[0]), comoProfessor(),
                "{\"rodadaId\":" + rodadaA2.getId() + ",\"prazo\":\"" + LocalDate.now(ClockConfig.FUSO_LOGICUP).minusDays(1) + "\"}")
                .andExpect(status().isBadRequest());                                                             // R12
        enviarJson(post("/api/turmas/{id}/tarefas", turma[0]), comoProfessor(),
                "{\"rodadaId\":" + Integer.MAX_VALUE + ",\"prazo\":null}").andExpect(status().isNotFound());
        enviarJson(post("/api/turmas/{id}/tarefas", turma[0]), comoProfessor(), "{\"prazo\":null}")
                .andExpect(status().isBadRequest());

        // prazo de HOJE é aceito
        criarTarefa(turma[0], rodadaA2, "\"" + LocalDate.now(ClockConfig.FUSO_LOGICUP) + "\"");
    }

    @Test
    void tarefaComPrazoVencidoApareceAtrasada() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma atrasada");
        entrar(ana, turma[1]).andExpect(status().isOk());
        // a API não aceita prazo no passado; aqui simulamos uma tarefa criada antes e que já venceu
        banco().update("INSERT INTO tarefa (id_turma, id_rodada, prazo) VALUES (?, ?, ?)",
                Integer.valueOf(turma[0]), rodadaA1.getId(), java.sql.Date.valueOf(LocalDate.now(ClockConfig.FUSO_LOGICUP).minusDays(2)));

        mockMvc.perform(get("/api/minha-turma").with(como(ana)))
                .andExpect(jsonPath("$.tarefas[0].status").value("PENDENTE"))
                .andExpect(jsonPath("$.tarefas[0].atrasada").value(true));

        aprovarA1(ana); // R12: depois do prazo ainda dá para fazer; concluída deixa de ser "atrasada"
        mockMvc.perform(get("/api/minha-turma").with(como(ana)))
                .andExpect(jsonPath("$.tarefas[0].status").value("CONCLUIDA"))
                .andExpect(jsonPath("$.tarefas[0].atrasada").value(false));
    }

    @Test
    void removerTarefaNaoApagaProgresso() throws Exception {
        String[] turma = criarTurma(comoProfessor(), "Turma remover tarefa");
        entrar(ana, turma[1]).andExpect(status().isOk());
        String tarefaId = criarTarefa(turma[0], rodadaA1, "null");
        aprovarA1(ana);

        semCorpo(delete("/api/turmas/{id}/tarefas/{tarefa}", turma[0], tarefaId), comoProfessor())
                .andExpect(status().isNoContent());
        semCorpo(delete("/api/turmas/{id}/tarefas/{tarefa}", turma[0], tarefaId), comoProfessor())
                .andExpect(status().isNotFound());

        assertEquals(1, banco().queryForObject(
                "SELECT COUNT(*) FROM progresso_exercicio WHERE id_usuario = ? AND status = 'CONCLUIDO'",
                Integer.class, ana.getId()));
        mockMvc.perform(get("/api/minha-turma").with(como(ana))).andExpect(jsonPath("$.tarefas.length()").value(0));
    }

    @Test
    void tarefaDeOutraTurmaNaoPodeSerRemovida() throws Exception {
        String[] minha = criarTurma(comoProfessor(), "Minha turma");
        String[] outra = criarTurma(comoOutroProfessor(), "Turma alheia");
        String tarefaAlheia;
        {
            String corpo = enviarJson(post("/api/turmas/{id}/tarefas", outra[0]), comoOutroProfessor(),
                    "{\"rodadaId\":" + rodadaA1.getId() + ",\"prazo\":null}")
                    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
            tarefaAlheia = String.valueOf((Integer) JsonPath.read(corpo, "$.id"));
        }
        // pela MINHA turma, tentando apagar a tarefa da turma dele
        semCorpo(delete("/api/turmas/{id}/tarefas/{tarefa}", minha[0], tarefaAlheia), comoProfessor())
                .andExpect(status().isNotFound());
        assertEquals(1, banco().queryForObject("SELECT COUNT(*) FROM tarefa WHERE id_tarefa = ?",
                Integer.class, Integer.valueOf(tarefaAlheia)));
    }

    @Test
    void escritaSemTokenCsrfERecusada() throws Exception {
        // Login de VERDADE (cookie com o JWT assinado), e não jwt(): o jwt() do spring-security-test
        // chama CsrfFilter.skipRequest() e DESLIGA a checagem de CSRF na requisição de teste.
        Cookie login = new Cookie(TokenService.NOME_COOKIE, tokenService.gerarToken(professor, true));

        mockMvc.perform(post("/api/turmas").cookie(login)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Sem CSRF\"}"))
                .andExpect(status().isForbidden());
        assertEquals(0, banco().queryForObject("SELECT COUNT(*) FROM turma WHERE id_professor = ?",
                Integer.class, professor.getId()));
    }

    @Test
    void comCookieDeLoginETokenCsrfCriaTurma() throws Exception {
        Cookie login = new Cookie(TokenService.NOME_COOKIE, tokenService.gerarToken(professor, true));

        mockMvc.perform(post("/api/turmas").cookie(login).with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Com CSRF\"}"))
                .andExpect(status().isCreated());
    }
}
