package br.com.logicup.backend.controller;

import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Dica;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.Modulo;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.TipoExercicio;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.DicaRepository;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ModuloRepository;
import br.com.logicup.backend.repository.ProgressoExercicioRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de INTEGRAÇÃO da trilha e das rodadas (T5: API testada antes do React).
 *
 * Monta uma trilha própria de teste com fases de ordenação NEGATIVA (-200 e -100):
 * assim elas vêm ANTES da Fase 1 real do seed, e os testes não dependem do conteúdo do seed.
 * @Transactional: tudo é desfeito no fim de cada teste.
 *
 * Login: jwt() cria um token "de mentira" já validado (pula a assinatura), com o id do usuário
 * no subject e o perfil na claim "role" — exatamente o que o TokenService coloca no token real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TrilhaRodadaIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private DicaRepository dicaRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private ProgressoExercicioRepository progressoRepository;

    private static final String CONTEUDO = """
            {"objetivos":["Objetivo de teste"],"teoria":[{"titulo":"T","texto":"Texto"}],"exemplos":[]}""";

    // Fase A (-200): rodada 1 (Q1 escolha única 10xp, Q2 texto livre 15xp, Q3 Boss resposta curta 20xp) e rodada 2
    // Fase B (-100): rodada 1
    private Rodada rodadaA1;
    private Rodada rodadaA2;
    private Rodada rodadaB1;
    private Exercicio q1;
    private Exercicio q2;
    private Exercicio q3;
    private Exercicio exercicioDaA2;
    private Aluno aluno;

    @BeforeEach
    void montarTrilhaDeTeste() {
        Modulo faseA = moduloRepository.save(new Modulo("Fase de teste A", "Teste", -200));
        Modulo faseB = moduloRepository.save(new Modulo("Fase de teste B", "Teste", -100));
        rodadaA1 = rodadaRepository.save(new Rodada(faseA, "Rodada A1", 1, CONTEUDO));
        rodadaA2 = rodadaRepository.save(new Rodada(faseA, "Rodada A2", 2, CONTEUDO));
        rodadaB1 = rodadaRepository.save(new Rodada(faseB, "Rodada B1", 1, CONTEUDO));

        q1 = exercicio(rodadaA1, "IT.A1.Q1", 1, TipoExercicio.MULTIPLA_ESCOLHA, FormatoResposta.ESCOLHA_UNICA, 10,
                "[{\"id\":\"a\",\"texto\":\"Sim\"},{\"id\":\"b\",\"texto\":\"Não\"}]",
                "{\"correta\":\"a\"}");
        q2 = exercicio(rodadaA1, "IT.A1.Q2", 2, TipoExercicio.ESCREVER_CODIGO, FormatoResposta.TEXTO_LIVRE, 15,
                null,
                "{\"palavrasChave\":[[\"lig\"],[\"carro\",\"motor\"]],\"minimo\":2,\"respostaModelo\":\"Ligar o carro\"}");
        q3 = exercicio(rodadaA1, "IT.A1.Q3", 3, TipoExercicio.BOSS_CHALLENGE, FormatoResposta.RESPOSTA_CURTA, 20,
                null,
                "{\"aceitas\":[\"Excluir\"],\"diferenciarMaiusculas\":false}");
        exercicioDaA2 = exercicio(rodadaA2, "IT.A2.Q1", 1, TipoExercicio.MULTIPLA_ESCOLHA,
                FormatoResposta.ESCOLHA_UNICA, 10, "[{\"id\":\"a\",\"texto\":\"Sim\"}]", "{\"correta\":\"a\"}");
        exercicio(rodadaB1, "IT.B1.Q1", 1, TipoExercicio.MULTIPLA_ESCOLHA,
                FormatoResposta.ESCOLHA_UNICA, 10, "[{\"id\":\"a\",\"texto\":\"Sim\"}]", "{\"correta\":\"a\"}");

        dicaRepository.save(new Dica(q1, "Pense bem."));

        aluno = alunoRepository.save(new Aluno("Aluno Teste", "aluno.trilha@teste.com", "hash-qualquer"));
    }

    private Exercicio exercicio(Rodada rodada, String codigo, int ordem, TipoExercicio tipo, FormatoResposta formato,
                                int pontos, String alternativas, String gabarito) {
        Exercicio exercicio = new Exercicio(rodada, codigo, "Questão " + codigo, ordem, tipo, formato, pontos,
                "Enunciado de teste", gabarito, "Explicação de teste");
        exercicio.setAlternativasJson(alternativas);
        return exercicioRepository.save(exercicio);
    }

    // ---------- helpers de login e de envio ----------

    private static RequestPostProcessor logadoComo(Integer id, String role) {
        return jwt().jwt(token -> token.subject(String.valueOf(id)).claim("role", role))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private RequestPostProcessor comoAluno() {
        return logadoComo(aluno.getId(), "ALUNO");
    }

    private static String item(Exercicio exercicio, String respostaJson) {
        return "{\"exercicioId\":%d,\"resposta\":%s}".formatted(exercicio.getId(), respostaJson);
    }

    private ResultActions enviar(Rodada rodada, RequestPostProcessor usuario, String... itens) throws Exception {
        return mockMvc.perform(post("/api/rodadas/{id}/respostas", rodada.getId())
                .with(usuario).with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"respostas\":[" + String.join(",", itens) + "]}"));
    }

    private static final String Q1_CERTA = "{\"alternativa\":\"a\"}";
    private static final String Q1_ERRADA = "{\"alternativa\":\"b\"}";
    private static final String Q2_CERTA = "{\"texto\":\"Primeiro liga o carro\"}";
    private static final String Q2_ERRADA = "{\"texto\":\"Dirigir\"}";
    private static final String Q3_CERTA = "{\"texto\":\"excluir\"}";

    // ---------- GET /api/trilha ----------

    @Test
    void semLoginRecebe401() throws Exception {
        mockMvc.perform(get("/api/trilha")).andExpect(status().isUnauthorized());
    }

    @Test
    void alunoNovoTemSoAPrimeiraRodadaLiberada() throws Exception {
        mockMvc.perform(get("/api/trilha").with(comoAluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aluno.xp").value(0))
                .andExpect(jsonPath("$.aluno.nivel").value(1))
                .andExpect(jsonPath("$.aluno.streak").value(0))
                .andExpect(jsonPath("$.fases[0].ordem").value(-200))
                .andExpect(jsonPath("$.fases[0].rodadas[0].status").value("LIBERADA"))
                .andExpect(jsonPath("$.fases[0].rodadas[0].totalExercicios").value(3))
                .andExpect(jsonPath("$.fases[0].rodadas[0].xpTotal").value(45))
                .andExpect(jsonPath("$.fases[0].rodadas[1].status").value("BLOQUEADA"))
                .andExpect(jsonPath("$.fases[1].ordem").value(-100))
                .andExpect(jsonPath("$.fases[1].rodadas[0].status").value("BLOQUEADA"));
    }

    @Test
    void professorVeTudoLiberadoESemResumoDeAluno() throws Exception {
        RequestPostProcessor professor = logadoComo(999999, "PROFESSOR");

        mockMvc.perform(get("/api/trilha").with(professor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aluno").value(nullValue()))
                .andExpect(jsonPath("$.fases[0].rodadas[1].status").value("LIBERADA"))
                .andExpect(jsonPath("$.fases[1].rodadas[0].status").value("LIBERADA"));

        mockMvc.perform(get("/api/rodadas/{id}", rodadaB1.getId()).with(professor))
                .andExpect(status().isOk()); // T3: vê o conteúdo...
    }

    @Test
    void professorNaoPodeResponder() throws Exception {
        enviar(rodadaA1, logadoComo(999999, "PROFESSOR"), item(q1, Q1_CERTA))
                .andExpect(status().isForbidden()); // ...mas não responde (T3)
    }

    // ---------- GET /api/rodadas/{id} ----------

    @Test
    void abrirRodadaNaoExpoeGabarito() throws Exception {
        mockMvc.perform(get("/api/rodadas/{id}", rodadaA1.getId()).with(comoAluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.objetivos[0]").value("Objetivo de teste"))
                .andExpect(jsonPath("$.exercicios.length()").value(3))
                .andExpect(jsonPath("$.exercicios[0].alternativas[1].texto").value("Não"))
                .andExpect(jsonPath("$.exercicios[0].dicas[0]").value("Pense bem."))
                .andExpect(jsonPath("$.exercicios[0].concluido").value(false))
                // nada do gabarito pode aparecer na resposta
                .andExpect(content().string(not(containsString("correta"))))
                .andExpect(content().string(not(containsString("gabarito"))))
                .andExpect(content().string(not(containsString("aceitas"))))
                .andExpect(content().string(not(containsString("palavrasChave"))))
                .andExpect(content().string(not(containsString("respostaModelo"))));
    }

    @Test
    void rodadaBloqueadaRecebe403() throws Exception {
        mockMvc.perform(get("/api/rodadas/{id}", rodadaA2.getId()).with(comoAluno()))
                .andExpect(status().isForbidden());
        enviar(rodadaA2, comoAluno(), item(exercicioDaA2, Q1_CERTA))
                .andExpect(status().isForbidden());
    }

    @Test
    void rodadaInexistenteRecebe404() throws Exception {
        mockMvc.perform(get("/api/rodadas/{id}", Integer.MAX_VALUE).with(comoAluno()))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /api/rodadas/{id}/respostas ----------

    @Test
    void envioCorrigeDaXpEMostraARespostaCerta() throws Exception {
        enviar(rodadaA1, comoAluno(), item(q1, Q1_CERTA), item(q2, Q2_ERRADA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados[0].correta").value(true))
                .andExpect(jsonPath("$.resultados[0].xpGanho").value(10))
                .andExpect(jsonPath("$.resultados[0].respostaCorreta.texto").value("Sim"))
                .andExpect(jsonPath("$.resultados[1].correta").value(false))
                .andExpect(jsonPath("$.resultados[1].xpGanho").value(0))
                .andExpect(jsonPath("$.resultados[1].explicacao").value("Explicação de teste"))
                .andExpect(jsonPath("$.resultados[1].respostaCorreta").value("Ligar o carro")) // só a resposta modelo
                .andExpect(jsonPath("$.resumo.acertos").value(1))
                .andExpect(jsonPath("$.resumo.xpGanho").value(10))
                .andExpect(jsonPath("$.resumo.exerciciosConcluidos").value(1))
                .andExpect(jsonPath("$.resumo.rodadaAprovada").value(false))
                .andExpect(jsonPath("$.resumo.proximaRodadaLiberadaId").value(nullValue()))
                .andExpect(jsonPath("$.resumo.aluno.xp").value(10))
                .andExpect(jsonPath("$.resumo.aluno.streak").value(1));

        // o progresso ficou gravado: 1 tentativa em cada questão enviada
        assertEquals(1, progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), q1.getId())
                .orElseThrow().getTentativas());
        assertEquals(1, progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), q2.getId())
                .orElseThrow().getTentativas());
    }

    @Test
    void reenviarQuestaoJaAcertadaNaoDaXpDeNovo() throws Exception {
        enviar(rodadaA1, comoAluno(), item(q1, Q1_CERTA)).andExpect(jsonPath("$.resumo.xpGanho").value(10));

        enviar(rodadaA1, comoAluno(), item(q1, Q1_CERTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados[0].correta").value(true))
                .andExpect(jsonPath("$.resultados[0].xpGanho").value(0)) // T1: XP só no primeiro acerto
                .andExpect(jsonPath("$.resumo.aluno.xp").value(10));

        // errar depois de já ter acertado também não "desconclui" (PR1)
        enviar(rodadaA1, comoAluno(), item(q1, Q1_ERRADA))
                .andExpect(jsonPath("$.resultados[0].correta").value(false))
                .andExpect(jsonPath("$.resumo.exerciciosConcluidos").value(1));

        assertEquals(3, progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), q1.getId())
                .orElseThrow().getTentativas()); // PR3: toda tentativa conta
    }

    @Test
    void acertarDepoisDeErrarDaMetadeDoXp() throws Exception { // PR7
        enviar(rodadaA1, comoAluno(), item(q2, Q2_ERRADA))
                .andExpect(jsonPath("$.resultados[0].xpGanho").value(0));

        enviar(rodadaA1, comoAluno(), item(q2, Q2_CERTA))
                .andExpect(jsonPath("$.resultados[0].correta").value(true))
                .andExpect(jsonPath("$.resultados[0].xpGanho").value(7))  // 15 / 2 = 7
                .andExpect(jsonPath("$.resumo.aluno.xp").value(7));

        // a trilha mostra o XP realmente ganho, não os 15 "de tabela"
        mockMvc.perform(get("/api/trilha").with(comoAluno()))
                .andExpect(jsonPath("$.aluno.xp").value(7))
                .andExpect(jsonPath("$.fases[0].rodadas[0].xpGanho").value(7));

        assertEquals(7, progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), q2.getId())
                .orElseThrow().getPontosGanhos());
    }

    @Test
    void semOBossARodadaNaoEhAprovada() throws Exception {
        enviar(rodadaA1, comoAluno(), item(q1, Q1_CERTA), item(q2, Q2_CERTA))
                .andExpect(jsonPath("$.resumo.exerciciosConcluidos").value(2))
                .andExpect(jsonPath("$.resumo.rodadaAprovada").value(false));
    }

    @Test
    void aprovarRodadaLiberaAProxima() throws Exception {
        enviar(rodadaA1, comoAluno(), item(q1, Q1_CERTA), item(q2, Q2_CERTA), item(q3, Q3_CERTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.xpGanho").value(45))
                .andExpect(jsonPath("$.resumo.rodadaAprovada").value(true))
                .andExpect(jsonPath("$.resumo.proximaRodadaLiberadaId").value(rodadaA2.getId()));

        mockMvc.perform(get("/api/trilha").with(comoAluno()))
                .andExpect(jsonPath("$.aluno.xp").value(45))
                .andExpect(jsonPath("$.fases[0].rodadas[0].status").value("APROVADA"))
                .andExpect(jsonPath("$.fases[0].rodadas[0].xpGanho").value(45))
                .andExpect(jsonPath("$.fases[0].rodadas[1].status").value("LIBERADA"))
                .andExpect(jsonPath("$.fases[1].rodadas[0].status").value("BLOQUEADA"));

        mockMvc.perform(get("/api/rodadas/{id}", rodadaA2.getId()).with(comoAluno()))
                .andExpect(status().isOk());
    }

    @Test
    void envioDeSubconjuntoEhPermitido() throws Exception { // T2
        enviar(rodadaA1, comoAluno(), item(q3, Q3_CERTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.totalEnviado").value(1))
                .andExpect(jsonPath("$.resumo.totalExercicios").value(3));
    }

    @Test
    void exercicioDeOutraRodadaRecebe400() throws Exception {
        enviar(rodadaA1, comoAluno(), item(exercicioDaA2, Q1_CERTA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void exercicioRepetidoNoMesmoEnvioRecebe400() throws Exception {
        enviar(rodadaA1, comoAluno(), item(q1, Q1_CERTA), item(q1, Q1_ERRADA))
                .andExpect(status().isBadRequest());
    }

    @Test
    void respostaNoFormatoErradoRecebe400() throws Exception {
        enviar(rodadaA1, comoAluno(), item(q1, "{\"texto\":\"a\"}")) // escolha única espera "alternativa"
                .andExpect(status().isBadRequest());
    }

    @Test
    void envioVazioRecebe400() throws Exception {
        enviar(rodadaA1, comoAluno()).andExpect(status().isBadRequest());
    }
}
