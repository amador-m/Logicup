package br.com.logicup.backend.execucao;

import br.com.logicup.backend.exception.ExecutorIndisponivelException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 9 (ADR-001): testes do ExecutorPiston contra um PISTON FALSO.
 *
 * O "Piston falso" é um servidor HTTP de verdade (com.sun.net.httpserver, que já vem no JDK) numa
 * porta livre do próprio computador. Ele responde o que cada teste mandar. Assim testamos o HTTP de
 * verdade (JSON enviado, timeout, erro 500, conexão recusada) sem Docker e sem o Piston real.
 *
 * O teste com o Piston REAL fica para o roteiro manual com o Docker (passo a passo à parte).
 */
class ExecutorPistonTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private HttpServer pistonFalso;
    /** Corpo da última requisição recebida pelo Piston falso. */
    private final AtomicReference<String> pedidoRecebido = new AtomicReference<>();
    /** Cabeçalho "Upgrade" da última requisição (precisa ser null: ver o teste naoPedeUpgradeParaHttp2). */
    private final AtomicReference<String> cabecalhoUpgrade = new AtomicReference<>();
    /** O que o Piston falso vai responder: status HTTP + corpo. */
    private volatile int statusHttp = 200;
    private volatile String corpoResposta = "{}";
    /** Se não for null, o Piston falso espera este "sinal" antes de responder (simula um Piston lento). */
    private volatile CountDownLatch segurarResposta;

    @BeforeEach
    void subirPistonFalso() throws IOException {
        pistonFalso = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0); // porta 0 = qualquer porta livre
        pistonFalso.createContext("/api/v2/execute", troca -> {
            pedidoRecebido.set(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            cabecalhoUpgrade.set(troca.getRequestHeaders().getFirst("Upgrade"));
            if (segurarResposta != null) {
                try {
                    segurarResposta.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] corpo = corpoResposta.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");
            troca.sendResponseHeaders(statusHttp, corpo.length);
            troca.getResponseBody().write(corpo);
            troca.close();
        });
        pistonFalso.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        pistonFalso.start();
    }

    @AfterEach
    void derrubarPistonFalso() {
        if (segurarResposta != null) segurarResposta.countDown();
        pistonFalso.stop(0);
    }

    // ---------------- helpers ----------------

    private String url() {
        return "http://127.0.0.1:" + pistonFalso.getAddress().getPort();
    }

    private ExecutorPiston executor(int simultaneas, Duration esperaNaFila) {
        return new ExecutorPiston(new ParametrosExecutor(url(), "15.0.2", Duration.ofSeconds(6), Duration.ofSeconds(5),
                256L * 1024 * 1024, simultaneas, esperaNaFila), jsonMapper);
    }

    private ExecutorPiston executor() {
        return executor(3, Duration.ofSeconds(1));
    }

    /** Monta a resposta do Piston no formato real da API dele. */
    private void pistonResponde(String stdout, String stderr, Integer code, String status) {
        corpoResposta = """
                {"language":"java","version":"15.0.2",
                 "run":{"stdout":%s,"stderr":%s,"code":%s,"signal":null,"status":%s,"message":null,"wall_time":812}}
                """.formatted(jsonMapper.writeValueAsString(stdout), jsonMapper.writeValueAsString(stderr),
                code, status == null ? "null" : "\"" + status + "\"");
    }

    private static final ProgramaJava OLA = ProgramaJava.semEntrada("""
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Olá, Gattuccino!");
                }
            }""");

    // ---------------- o pedido enviado ----------------

    @Test
    void enviaOCodigoAVersaoEOsLimitesParaOPiston() {
        pistonResponde("Olá, Gattuccino!\n", "", 0, null);

        executor().executar(new ProgramaJava(OLA.codigoFonte(), "42"));

        JsonNode pedido = jsonMapper.readTree(pedidoRecebido.get());
        assertEquals("java", pedido.get("language").asString());
        assertEquals("15.0.2", pedido.get("version").asString());
        // SEM ".java": o Piston acrescenta a extensão sozinho (senão vira "Main.java.java")
        assertEquals("Main", pedido.get("files").get(0).get("name").asString());
        assertEquals(OLA.codigoFonte(), pedido.get("files").get(0).get("content").asString());
        assertEquals("42", pedido.get("stdin").asString());
        // os limites vão em TODA requisição (o padrão de memória do Piston é ilimitado)
        assertEquals(6000, pedido.get("run_timeout").asLong());
        assertEquals(5000, pedido.get("run_cpu_time").asLong());
        assertEquals(256L * 1024 * 1024, pedido.get("run_memory_limit").asLong());
    }

    /**
     * Bug encontrado no teste com o Piston REAL (02/11): o HttpClient do Java, por padrão, pede para trocar
     * para HTTP/2 ("Upgrade: h2c"), e o Piston responde 400 a qualquer "Upgrade" que não seja WebSocket.
     * Este servidor falso aceitava o pedido mesmo assim, então o bug passou. Agora o teste confere o cabeçalho.
     */
    @Test
    void naoPedeUpgradeParaHttp2() {
        pistonResponde("ok\n", "", 0, null);
        executor().executar(OLA);
        assertNull(cabecalhoUpgrade.get(), "o pedido não pode ter o cabeçalho Upgrade (o Piston responde 400)");
    }

    // ---------------- tradução das respostas do Piston ----------------

    @Test
    void programaQueTerminaNormalmenteEhSucesso() {
        pistonResponde("Olá, Gattuccino!\n", "", 0, null);

        ResultadoExecucao resultado = executor().executar(OLA);

        assertEquals(SituacaoExecucao.SUCESSO, resultado.situacao());
        assertEquals("Olá, Gattuccino!\n", resultado.saida());
        assertEquals(812L, resultado.tempoMs());
    }

    @Test
    void erroDeCompilacaoEhReconhecidoEOCaminhoInternoSome() {
        // mensagem real do Java no modo arquivo único, com o caminho da pasta temporária do Piston
        pistonResponde("", """
                /piston/jobs/3f2a9c1e-77aa-4b1c-9d0e-1a2b3c4d5e6f/Main.java:3: error: ';' expected
                        System.out.println("oi")
                                                ^
                1 error
                error: compilation failed
                """, 1, "RE");

        ResultadoExecucao resultado = executor().executar(OLA);

        assertEquals(SituacaoExecucao.ERRO_COMPILACAO, resultado.situacao());
        assertTrue(resultado.erros().startsWith("Main.java:3: error: ';' expected"), resultado.erros());
        assertFalse(resultado.erros().contains("/piston/"), "não mostra caminhos internos do servidor");
    }

    @Test
    void excecaoDuranteAExecucaoEhErroDeExecucao() {
        pistonResponde("", """
                Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 2
                \tat Main.main(Main.java:3)
                """, 1, "RE");

        assertEquals(SituacaoExecucao.ERRO_EXECUCAO, executor().executar(OLA).situacao());
    }

    @Test
    void lacoInfinitoViraTempoEsgotado() {
        pistonResponde("", "", null, "TO");
        assertEquals(SituacaoExecucao.TEMPO_ESGOTADO, executor().executar(OLA).situacao());
    }

    @Test
    void saidaGrandeDemaisViraSaidaExcedida() {
        pistonResponde("1\n1\n1\n", "", null, "OL");
        assertEquals(SituacaoExecucao.SAIDA_EXCEDIDA, executor().executar(OLA).situacao());
    }

    @Test
    void processoMortoPorSinalViraInterrompido() {
        pistonResponde("", "", null, "SG");
        assertEquals(SituacaoExecucao.INTERROMPIDO, executor().executar(OLA).situacao());
    }

    // ---------------- executor indisponível (nunca culpa do aluno) ----------------

    @Test
    void erroInternoDoPistonDeixaOExecutorIndisponivel() {
        pistonResponde("", "", null, "XX");
        assertThrows(ExecutorIndisponivelException.class, () -> executor().executar(OLA));
    }

    @Test
    void pistonRespondendoErro500DeixaOExecutorIndisponivel() {
        statusHttp = 500;
        corpoResposta = "{\"message\":\"internal error\"}";
        assertThrows(ExecutorIndisponivelException.class, () -> executor().executar(OLA));
    }

    @Test
    void pistonDesligadoDeixaOExecutorIndisponivel() throws IOException {
        int portaFechada;
        try (ServerSocket socket = new ServerSocket(0)) { // pega uma porta livre e a fecha em seguida
            portaFechada = socket.getLocalPort();
        }
        ExecutorPiston semPiston = new ExecutorPiston(new ParametrosExecutor("http://127.0.0.1:" + portaFechada,
                "15.0.2", Duration.ofSeconds(6), Duration.ofSeconds(5), 256L * 1024 * 1024, 3, Duration.ofSeconds(1)),
                jsonMapper);

        assertThrows(ExecutorIndisponivelException.class, () -> semPiston.executar(OLA));
    }

    @Test
    void pistonTravadoNaoPrendeARequisicaoParaSempre() {
        segurarResposta = new CountDownLatch(1); // o Piston falso só responde quando o teste mandar (nunca, aqui)
        ExecutorPiston comTimeoutCurto = new ExecutorPiston(new ParametrosExecutor(url(), "15.0.2",
                Duration.ofMillis(200), Duration.ofMillis(200), 256L * 1024 * 1024, 3, Duration.ofSeconds(1)),
                jsonMapper, Duration.ofMillis(300));

        long inicio = System.nanoTime();
        assertThrows(ExecutorIndisponivelException.class, () -> comTimeoutCurto.executar(OLA));
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        assertTrue(ms < 3000, "desistiu em " + ms + " ms (timeout de 500 ms)");
    }

    @Test
    void comAFilaCheiaOPedidoDesisteEmVezDeEsperarParaSempre() throws Exception {
        pistonResponde("ok\n", "", 0, null);
        segurarResposta = new CountDownLatch(1);
        ExecutorPiston umaVaga = executor(1, Duration.ofMillis(200)); // só 1 execução por vez

        // 1º pedido: ocupa a única vaga (o Piston falso está "segurando" a resposta)
        CompletableFuture<ResultadoExecucao> primeiro = CompletableFuture.supplyAsync(() -> umaVaga.executar(OLA));
        Thread.sleep(300);

        // 2º pedido: não há vaga; espera 200 ms e desiste
        ExecutorIndisponivelException erro =
                assertThrows(ExecutorIndisponivelException.class, () -> umaVaga.executar(OLA));
        assertTrue(erro.getMessage().contains("Muitos programas"));

        // o 1º termina normalmente e DEVOLVE a vaga: um 3º pedido consegue rodar
        segurarResposta.countDown();
        assertTrue(primeiro.get(5, TimeUnit.SECONDS).sucesso());
        segurarResposta = null;
        assertTrue(umaVaga.executar(OLA).sucesso());
    }

    // ---------------- utilidades ----------------

    @Test
    void textoMuitoGrandeEhCortado() {
        String enorme = "x".repeat(ExecutorPiston.LIMITE_TEXTO + 500);
        String cortado = ExecutorPiston.limitar(enorme);
        assertTrue(cortado.length() < enorme.length());
        assertTrue(cortado.endsWith("[... saída cortada ...]"));
    }

    @Test
    void parametrosInvalidosSaoRecusados() {
        assertThrows(IllegalArgumentException.class, () -> new ParametrosExecutor("", "15.0.2",
                Duration.ofSeconds(1), Duration.ofSeconds(1), 256L * 1024 * 1024, 1, Duration.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new ParametrosExecutor(url(), "15.0.2",
                Duration.ofSeconds(1), Duration.ofSeconds(1), 1024, 1, Duration.ZERO)); // memória pequena demais
        assertThrows(IllegalArgumentException.class, () -> new ParametrosExecutor(url(), "15.0.2",
                Duration.ofSeconds(1), Duration.ofSeconds(1), 256L * 1024 * 1024, 0, Duration.ZERO)); // 0 vagas
    }
}
