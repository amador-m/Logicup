package br.com.logicup.backend.controller;

import br.com.logicup.backend.exception.ExecutorIndisponivelException;
import br.com.logicup.backend.execucao.ExecutorDeCodigo;
import br.com.logicup.backend.execucao.ExecutorFalso;
import br.com.logicup.backend.execucao.ResultadoExecucao;
import br.com.logicup.backend.execucao.SituacaoExecucao;
import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.security.TokenService;
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

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Sprint 9 (ADR-001): POST /api/execucoes de ponta a ponta (HTTP -> Controller -> Service -> executor).
 *
 * O executor aqui é o ExecutorFalso (marcado @Primary, então o Spring o prefere ao ExecutorDesligado):
 * testamos as NOSSAS regras (login, validação, limite, 503) sem depender do Piston.
 * Limite de execuções baixo (3) só nesta classe; cada teste usa um usuário diferente.
 */
@SpringBootTest(properties = "logicup.limites.execucao.maximo=3")
@AutoConfigureMockMvc
@Import(ExecucaoIntegrationTest.ExecutorDeTeste.class)
class ExecucaoIntegrationTest {

    @TestConfiguration
    static class ExecutorDeTeste {
        @Bean
        @Primary
        ExecutorFalso executorFalso() {
            return new ExecutorFalso();
        }
    }

    private static final String PROGRAMA = """
            public class Main { public static void main(String[] a) { System.out.println("oi"); } }""";

    @Autowired private MockMvc mockMvc;
    @Autowired private ExecutorFalso executorFalso;
    @Autowired private ExecutorDeCodigo executorInjetado;

    @BeforeEach
    void executorRespondeSucesso() {
        executorFalso.recebidos().clear();
        executorFalso.quandoExecutar(p -> new ResultadoExecucao(SituacaoExecucao.SUCESSO, "oi\n", "", 0, 700L));
    }

    private static RequestPostProcessor logado(int id, String role) {
        return jwt().jwt(t -> t.subject(String.valueOf(id)).claim("role", role)
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private ResultActions executar(RequestPostProcessor usuario, String corpoJson) throws Exception {
        var requisicao = post("/api/execucoes").with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content(corpoJson);
        if (usuario != null) requisicao = requisicao.with(usuario);
        return mockMvc.perform(requisicao);
    }

    private static String corpo(String codigo) {
        return "{\"codigo\": %s, \"entrada\": \"\"}".formatted(
                tools.jackson.databind.json.JsonMapper.builder().build().writeValueAsString(codigo));
    }

    // ----------------------------------------------------------------

    @Test
    void oSpringInjetaOExecutorFalsoNesteTeste() {
        assertEquals(ExecutorFalso.class, executorInjetado.getClass());
    }

    @Test
    void semLoginNaoExecuta() throws Exception {
        executar(null, corpo(PROGRAMA)).andExpect(status().isUnauthorized());
        assertEquals(0, executorFalso.recebidos().size(), "nem chegou ao executor");
    }

    @Test
    void alunoLogadoExecutaEVeASaida() throws Exception {
        executar(logado(9001, "ALUNO"), corpo(PROGRAMA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("SUCESSO"))
                .andExpect(jsonPath("$.saida").value("oi\n"))
                .andExpect(jsonPath("$.mensagem").value("Programa executado com sucesso."))
                .andExpect(jsonPath("$.tempoMs").value(700));
        assertEquals(PROGRAMA, executorFalso.recebidos().getFirst().codigoFonte());
    }

    @Test
    void professorTambemPodeUsarOLaboratorio() throws Exception {
        executar(logado(9002, "PROFESSOR"), corpo(PROGRAMA)).andExpect(status().isOk());
    }

    @Test
    void erroDeCompilacaoNaoEhErroHttp() throws Exception {
        // o código do aluno ter erro é uma resposta NORMAL (200), com a situação explicando
        executorFalso.quandoExecutar(p -> new ResultadoExecucao(SituacaoExecucao.ERRO_COMPILACAO, "",
                "Main.java:1: error: ';' expected", 1, 500L));
        executar(logado(9003, "ALUNO"), corpo(PROGRAMA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ERRO_COMPILACAO"))
                .andExpect(jsonPath("$.erros", containsString("';' expected")));
    }

    @Test
    void codigoVazioOuGrandeDemaisEhRecusadoAntesDeExecutar() throws Exception {
        executar(logado(9004, "ALUNO"), corpo("   ")).andExpect(status().isBadRequest());
        executar(logado(9004, "ALUNO"), corpo("x".repeat(10_001))).andExpect(status().isBadRequest());
        assertEquals(0, executorFalso.recebidos().size());
    }

    @Test
    void passarDoLimiteDeExecucoesDa429() throws Exception {
        for (int i = 0; i < 3; i++) {
            executar(logado(9005, "ALUNO"), corpo(PROGRAMA)).andExpect(status().isOk());
        }
        executar(logado(9005, "ALUNO"), corpo(PROGRAMA))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
        // o limite é POR USUÁRIO: outro aluno continua executando
        executar(logado(9006, "ALUNO"), corpo(PROGRAMA)).andExpect(status().isOk());
    }

    @Test
    void executorForaDoArDa503ComMensagemClara() throws Exception {
        executorFalso.quandoExecutar(p -> {
            throw new ExecutorIndisponivelException("A execução de código está temporariamente indisponível.");
        });
        executar(logado(9007, "ALUNO"), corpo(PROGRAMA))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().string("Retry-After", "30"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.mensagem", containsString("indisponível")));
    }
}
