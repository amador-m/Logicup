package br.com.logicup.backend.controller;

import br.com.logicup.backend.exception.ExecutorIndisponivelException;
import br.com.logicup.backend.execucao.ExecutorFalso;
import br.com.logicup.backend.execucao.ResultadoExecucao;
import br.com.logicup.backend.execucao.SituacaoExecucao;
import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.Modulo;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.TipoExercicio;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ModuloRepository;
import br.com.logicup.backend.repository.ProgressoExercicioRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import br.com.logicup.backend.security.TokenService;
import br.com.logicup.backend.service.correcao.ComparadorSaida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 9 (ADR-001): questão de CÓDIGO de ponta a ponta no "Enviar" e no "Executar" da questão,
 * com o executor FALSO (sem Docker). Confere as regras de XP e o estado PENDENTE (decisão 1-A).
 *
 * PRÉ-REQUISITO: banco com a migração 07a (o ENUM precisa aceitar 'CODIGO' para salvar a questão de teste).
 * Trilha própria de teste com ordenação -300, como no TrilhaRodadaIntegrationTest. @Transactional desfaz tudo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(QuestaoCodigoIntegrationTest.ExecutorDeTeste.class)
class QuestaoCodigoIntegrationTest {

    @TestConfiguration
    static class ExecutorDeTeste {
        @Bean
        @Primary
        ExecutorFalso executorFalso() {
            return new ExecutorFalso();
        }
    }

    private static final String M = ComparadorSaida.MARCA_DE_CASO;
    private static final String CONTEUDO = """
            {"objetivos":["Objetivo"],"teoria":[{"titulo":"T","texto":"Texto"}],"exemplos":[]}""";
    private static final String GABARITO = """
            {"palavrasChave": [["if"], ["println"]], "minimo": 2, "respostaModelo": "if (x > 0) ...",
             "modo": "TRECHO", "comparacao": "EXATA",
             "programaTeste": "public class Main {\\n    static void executar(int x) {\\n//@CODIGO_DO_ALUNO\\n    }\\n}\\n",
             "programaExemplo": "public class Main {\\n    static void executar(int x) {\\n//@CODIGO_DO_ALUNO\\n    }\\n    public static void main(String[] a) { executar(5); }\\n}\\n",
             "casos": [{"descricao": "x = 5", "saida": "positivo"}, {"descricao": "x = -1", "saida": "negativo"}]}""";
    private static final String CODIGO_DO_ALUNO =
            "{\"codigo\": \"if (x > 0) System.out.println(\\\"positivo\\\"); else System.out.println(\\\"negativo\\\");\"}";

    @Autowired private MockMvc mockMvc;
    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private ProgressoExercicioRepository progressoRepository;
    @Autowired private ExecutorFalso executor;

    private Rodada rodada1;
    private Rodada rodada2;
    private Exercicio questaoCodigo;
    private Exercicio questaoEscolha;
    private Exercicio questaoDaRodada2;
    private Exercicio questaoBlocos;
    private Aluno aluno;

    @BeforeEach
    void montar() {
        Modulo fase = moduloRepository.save(new Modulo("Fase de teste código", "Teste", -300));
        rodada1 = rodadaRepository.save(new Rodada(fase, "Rodada C1", 1, CONTEUDO));
        rodada2 = rodadaRepository.save(new Rodada(fase, "Rodada C2", 2, CONTEUDO));
        questaoCodigo = exercicioRepository.save(new Exercicio(rodada1, "IT.C1.Q1", "Código", 1,
                TipoExercicio.ESCREVER_CODIGO, FormatoResposta.CODIGO, 25, "Escreva...", GABARITO, "Explicação do código"));
        questaoEscolha = new Exercicio(rodada1, "IT.C1.Q2", "Escolha", 2, TipoExercicio.MULTIPLA_ESCOLHA,
                FormatoResposta.ESCOLHA_UNICA, 10, "Qual?", "{\"correta\":\"a\"}", "Explicação");
        questaoEscolha.setAlternativasJson("[{\"id\":\"a\",\"texto\":\"Sim\"},{\"id\":\"b\",\"texto\":\"Não\"}]");
        questaoEscolha = exercicioRepository.save(questaoEscolha);
        questaoDaRodada2 = exercicioRepository.save(new Exercicio(rodada2, "IT.C2.Q1", "Código 2", 1,
                TipoExercicio.ESCREVER_CODIGO, FormatoResposta.CODIGO, 25, "Escreva...", GABARITO, "Explicação"));
        // Sprint 9 (Blockly): questão de BLOCOS com o mesmo gabarito (é corrigida como código)
        questaoBlocos = exercicioRepository.save(new Exercicio(rodada1, "IT.C1.Q3", "Blocos", 3,
                TipoExercicio.BLOCOS, FormatoResposta.BLOCOS, 25, "Monte os blocos...", GABARITO, "Explicação dos blocos"));
        aluno = alunoRepository.save(new Aluno("Aluno Código", "aluno.codigo.teste", "aluno.codigo@teste.com", "hash"));
        executor.recebidos().clear();
        executorPassaNosCasos();
    }

    private void executorPassaNosCasos() {
        executor.quandoExecutar(p -> new ResultadoExecucao(SituacaoExecucao.SUCESSO, M + "positivo\n" + M + "negativo\n", "", 0, 700L));
    }

    private void executorForaDoAr() {
        executor.quandoExecutar(p -> { throw new ExecutorIndisponivelException("fora do ar"); });
    }

    private RequestPostProcessor comoAluno() {
        return jwt().jwt(t -> t.subject(String.valueOf(aluno.getId())).claim("role", "ALUNO")
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_ALUNO"));
    }

    private ResultActions enviar(Rodada rodada, String... itens) throws Exception {
        return mockMvc.perform(post("/api/rodadas/{id}/respostas", rodada.getId()).with(comoAluno()).with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content("{\"respostas\":[" + String.join(",", itens) + "]}"));
    }

    private static String item(Exercicio exercicio, String resposta) {
        return "{\"exercicioId\":%d,\"resposta\":%s}".formatted(exercicio.getId(), resposta);
    }

    // ---------------------------------------------------------------- Enviar

    @Test
    void codigoCertoDaXpEMostraOsTestes() throws Exception {
        enviar(rodada1, item(questaoCodigo, CODIGO_DO_ALUNO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados[0].correta").value(true))
                .andExpect(jsonPath("$.resultados[0].pendente").value(false))
                .andExpect(jsonPath("$.resultados[0].xpGanho").value(25))
                .andExpect(jsonPath("$.resultados[0].execucao.tipo").value("TESTES_OK"))
                .andExpect(jsonPath("$.resultados[0].respostaCorreta").value("if (x > 0) ..."))
                .andExpect(jsonPath("$.resumo.pendentes").value(0));
        assertEquals(1, executor.recebidos().size(), "executou UMA vez (todos os casos na mesma execução)");
    }

    @Test
    void codigoErradoContaTentativaEMostraOCaso() throws Exception {
        executor.quandoExecutar(p -> new ResultadoExecucao(SituacaoExecucao.SUCESSO, M + "positivo\n" + M + "positivo\n", "", 0, 700L));

        enviar(rodada1, item(questaoCodigo, CODIGO_DO_ALUNO))
                .andExpect(jsonPath("$.resultados[0].correta").value(false))
                .andExpect(jsonPath("$.resultados[0].execucao.tipo").value("SAIDA_DIFERENTE"))
                .andExpect(jsonPath("$.resultados[0].execucao.caso").value("x = -1"))
                .andExpect(jsonPath("$.resultados[0].execucao.esperado").value("negativo"));

        assertEquals(1, progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), questaoCodigo.getId())
                .orElseThrow().getTentativas());
    }

    @Test
    void executorForaDoArDeixaPendenteSemXpSemTentativaESemRespostaModelo() throws Exception {
        executorForaDoAr();

        enviar(rodada1, item(questaoCodigo, CODIGO_DO_ALUNO), item(questaoEscolha, "{\"alternativa\":\"a\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados[0].pendente").value(true))
                .andExpect(jsonPath("$.resultados[0].correta").value(false))
                .andExpect(jsonPath("$.resultados[0].xpGanho").value(0))
                .andExpect(jsonPath("$.resultados[0].respostaCorreta").doesNotExist())
                .andExpect(jsonPath("$.resultados[0].explicacao").doesNotExist())
                .andExpect(jsonPath("$.resultados[0].execucao.tipo").value("EXECUTOR_INDISPONIVEL"))
                .andExpect(jsonPath("$.resultados[0].execucao.mensagem", containsString("NÃO foi validado")))
                // a outra questão do mesmo envio é corrigida normalmente
                .andExpect(jsonPath("$.resultados[1].correta").value(true))
                .andExpect(jsonPath("$.resumo.pendentes").value(1))
                .andExpect(jsonPath("$.resumo.acertos").value(1));

        assertTrue(progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), questaoCodigo.getId()).isEmpty(),
                "pendente não grava progresso");
    }

    @Test
    void depoisDeUmaPendenciaOAcertoValeXpCheio() throws Exception {
        executorForaDoAr();
        enviar(rodada1, item(questaoCodigo, CODIGO_DO_ALUNO)).andExpect(jsonPath("$.resultados[0].pendente").value(true));

        executorPassaNosCasos(); // o executor voltou
        enviar(rodada1, item(questaoCodigo, CODIGO_DO_ALUNO))
                .andExpect(jsonPath("$.resultados[0].correta").value(true))
                .andExpect(jsonPath("$.resultados[0].xpGanho").value(25)); // não caiu pela metade: pendência não é erro
    }

    @Test
    void rodadaBloqueadaNemGastaExecucao() throws Exception {
        enviar(rodada2, item(questaoDaRodada2, CODIGO_DO_ALUNO)).andExpect(status().isForbidden());
        assertEquals(0, executor.recebidos().size());
    }

    @Test
    void respostaDeOutraRodadaNemGastaExecucao() throws Exception {
        enviar(rodada1, item(questaoDaRodada2, CODIGO_DO_ALUNO)).andExpect(status().isBadRequest());
        assertEquals(0, executor.recebidos().size());
    }

    // ---------------------------------------------------------------- Executar da questão

    @Test
    void executarDaQuestaoRodaOProgramaDeExemplo() throws Exception {
        executor.quandoExecutar(p -> new ResultadoExecucao(SituacaoExecucao.SUCESSO, "positivo\n", "", 0, 650L));

        mockMvc.perform(post("/api/exercicios/{id}/execucoes", questaoCodigo.getId()).with(comoAluno()).with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"System.out.println(\\\"positivo\\\");\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("SUCESSO"))
                .andExpect(jsonPath("$.saida").value("positivo\n"));

        String enviado = executor.recebidos().getFirst().codigoFonte();
        assertTrue(enviado.contains("executar(5)"), "usou o programa de EXEMPLO");
        assertFalse(enviado.contains(M), "o programa de exemplo não tem os casos ocultos");
    }

    @Test
    void executarSoServeParaQuestaoDeCodigo() throws Exception {
        mockMvc.perform(post("/api/exercicios/{id}/execucoes", questaoEscolha.getId()).with(comoAluno()).with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"codigo\": \"x\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/exercicios/{id}/execucoes", 999_999).with(comoAluno()).with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"codigo\": \"x\"}"))
                .andExpect(status().isNotFound());
        assertEquals(0, executor.recebidos().size());
    }

    @Test
    void executarComProgramaInteiroExplicaOQueEscrever() throws Exception {
        mockMvc.perform(post("/api/exercicios/{id}/execucoes", questaoCodigo.getId()).with(comoAluno()).with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"public class Main { public static void main(String[] a) {} }\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("só o trecho pedido")));
    }

    // ---------------------------------------------------------------- Blocos (Sprint 9, etapa 1 do Blockly)

    /** O navegador manda o Java gerado E o desenho dos blocos; o servidor corrige só o Java. */
    private static final String BLOCOS_DO_ALUNO = CODIGO_DO_ALUNO.replaceFirst("\\}$",
            ", \"blocos\": {\"blocks\": {\"languageVersion\": 0, \"blocks\": []}}}");

    @Test
    void questaoDeBlocosECorrigidaExecutandoOJavaGerado() throws Exception {
        enviar(rodada1, item(questaoBlocos, BLOCOS_DO_ALUNO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados[0].correta").value(true))
                .andExpect(jsonPath("$.resultados[0].xpGanho").value(25))
                .andExpect(jsonPath("$.resultados[0].execucao.tipo").value("TESTES_OK"))
                .andExpect(jsonPath("$.resultados[0].respostaCorreta").value("if (x > 0) ..."));
        assertEquals(1, executor.recebidos().size());
        assertTrue(executor.recebidos().getFirst().codigoFonte().contains("System.out.println(\"positivo\")"),
                "o Java gerado pelos blocos foi encaixado no programa de teste");
    }

    @Test
    void questaoDeBlocosComExecutorForaDoArFicaPendente() throws Exception {
        executorForaDoAr();
        enviar(rodada1, item(questaoBlocos, BLOCOS_DO_ALUNO))
                .andExpect(jsonPath("$.resultados[0].pendente").value(true))
                .andExpect(jsonPath("$.resultados[0].xpGanho").value(0));
        assertTrue(progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), questaoBlocos.getId()).isEmpty());
    }

    @Test
    void executarDaQuestaoTambemServeParaBlocos() throws Exception {
        executor.quandoExecutar(p -> new ResultadoExecucao(SituacaoExecucao.SUCESSO, "positivo\n", "", 0, 650L));
        mockMvc.perform(post("/api/exercicios/{id}/execucoes", questaoBlocos.getId()).with(comoAluno()).with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\": \"System.out.println(\\\"positivo\\\");\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("SUCESSO"));
    }
}
