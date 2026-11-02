package br.com.logicup.backend.execucao;

import br.com.logicup.backend.exception.ExecutorIndisponivelException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.json.JsonMapper;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Sprint 9 (ADR-001): executa código Java no Piston (self-hosted), pela API HTTP dele:
 *   POST {url}/api/v2/execute
 *
 * Camadas de proteção que ficam AQUI no backend (além do isolamento do próprio Piston):
 *  1. Limites em TODA requisição (tempo, CPU, memória). O Piston recusa valores acima do
 *     configurado no servidor dele, então este é o "segundo cadeado".
 *  2. Fila: no máximo N execuções ao mesmo tempo (Semaphore). Com 100 alunos clicando juntos,
 *     o servidor não cai: os pedidos esperam a vez. Se a espera passar do limite, desistimos com
 *     ExecutorIndisponivelException, em vez de deixar a requisição pendurada.
 *  3. Timeout HTTP: se o Piston travar, não ficamos esperando para sempre.
 *  4. Tamanho máximo do texto devolvido ao aluno.
 *
 * Esta classe NÃO conhece questões, XP nem alunos: só "rodar um programa".
 */
public class ExecutorPiston implements ExecutorDeCodigo {

    private static final Logger log = LoggerFactory.getLogger(ExecutorPiston.class);

    /**
     * Nome do arquivo que mandamos ao Piston, SEM extensão: o script "run" do pacote Java do Piston faz
     * "mv $1 $1.java", então ele mesmo acrescenta o ".java" (mandando "Main.java", virava "Main.java.java";
     * descoberto no teste com o Piston real, 02/11). No modo arquivo único, o nome não precisa bater com a classe.
     */
    static final String NOME_ARQUIVO = "Main";
    /** Como o arquivo aparece nas mensagens de erro do compilador. */
    static final String ARQUIVO_FONTE = NOME_ARQUIVO + ".java";
    /** Última linha que o Java escreve quando a compilação falha no modo arquivo único. */
    static final String MARCA_ERRO_COMPILACAO = "error: compilation failed";
    /** Texto máximo devolvido ao aluno (o Piston também limita a saída, na configuração dele). */
    static final int LIMITE_TEXTO = 20_000;
    /** Folga do timeout HTTP sobre o tempo de execução (rede, criar a sandbox, limpar). */
    static final Duration FOLGA_HTTP = Duration.ofSeconds(5);

    // ---------- formato JSON da API do Piston (nomes com "_" porque é assim que a API espera) ----------

    record Arquivo(String name, String content) {}

    record Pedido(String language, String version, List<Arquivo> files, String stdin,
                  @JsonProperty("run_timeout") long runTimeout,
                  @JsonProperty("run_cpu_time") long runCpuTime,
                  @JsonProperty("run_memory_limit") long runMemoryLimit) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Etapa(String stdout, String stderr, Integer code, String signal, String status, String message,
                 @JsonProperty("wall_time") Long wallTime) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Resposta(Etapa compile, Etapa run, String message) {}

    // -----------------------------------------------------------------------------------------------

    private final ParametrosExecutor parametros;
    private final RestClient http;
    private final JsonMapper jsonMapper;
    private final Semaphore vagas;

    public ExecutorPiston(ParametrosExecutor parametros, JsonMapper jsonMapper) {
        this(parametros, jsonMapper, FOLGA_HTTP);
    }

    /** Usado pelos testes para não esperar 5 s de folga no teste de "Piston travado". */
    ExecutorPiston(ParametrosExecutor parametros, JsonMapper jsonMapper, Duration folgaHttp) {
        this.parametros = parametros;
        this.jsonMapper = jsonMapper;
        this.vagas = new Semaphore(parametros.simultaneas(), true); // true = justo: quem chegou primeiro, roda primeiro

        // HTTP/1.1 OBRIGATÓRIO: o padrão do HttpClient é HTTP/2, que num endereço http:// manda o cabeçalho
        // "Upgrade: h2c". O Piston usa express-ws (WebSocket), que intercepta qualquer "Upgrade" e responde
        // 400 "Bad Request". Descoberto no teste com o Piston real (02/11); ver ExecutorPistonTest.
        HttpClient cliente = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(parametros.tempoExecucao().plus(folgaHttp));
        this.http = RestClient.builder().baseUrl(parametros.url()).requestFactory(fabrica).build();
    }

    @Override
    public ResultadoExecucao executar(ProgramaJava programa) {
        boolean conseguiuVaga;
        try {
            conseguiuVaga = vagas.tryAcquire(parametros.esperaNaFila().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // boa prática: não "engolir" a interrupção da thread
            throw new ExecutorIndisponivelException("A execução foi interrompida. Tente novamente.", e);
        }
        if (!conseguiuVaga) {
            log.warn("Executor: fila cheia ({} execuções simultâneas)", parametros.simultaneas());
            throw new ExecutorIndisponivelException(
                    "Muitos programas sendo executados agora. Aguarde alguns segundos e tente novamente.");
        }
        try {
            return traduzir(chamarPiston(programa));
        } finally {
            vagas.release(); // SEMPRE devolve a vaga, mesmo se deu erro (senão a fila "entope")
        }
    }

    private Resposta chamarPiston(ProgramaJava programa) {
        Pedido pedido = new Pedido("java", parametros.versaoJava(),
                List.of(new Arquivo(NOME_ARQUIVO, programa.codigoFonte())), programa.entrada(),
                parametros.tempoExecucao().toMillis(), parametros.tempoCpu().toMillis(), parametros.memoriaBytes());
        try {
            String json = http.post().uri("/api/v2/execute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonMapper.writeValueAsString(pedido))
                    .retrieve()
                    .body(String.class);
            return jsonMapper.readValue(json, Resposta.class);
        } catch (RestClientException e) {
            // não conectou, demorou demais ou o Piston respondeu 4xx/5xx
            log.warn("Executor: falha ao chamar o Piston: {}", e.getMessage());
            throw new ExecutorIndisponivelException(
                    "A execução de código está temporariamente indisponível. Tente novamente mais tarde.", e);
        } catch (RuntimeException e) {
            // resposta que não é o JSON esperado (versão do Piston diferente?)
            log.error("Executor: resposta inesperada do Piston", e);
            throw new ExecutorIndisponivelException(
                    "A execução de código está temporariamente indisponível. Tente novamente mais tarde.", e);
        }
    }

    /**
     * Traduz a resposta do Piston para o vocabulário do LogiCup (SituacaoExecucao).
     * Package-private e static para ser testada diretamente, sem HTTP.
     */
    static ResultadoExecucao traduzir(Resposta resposta) {
        Etapa run = resposta == null ? null : resposta.run();
        if (run == null) {
            throw new ExecutorIndisponivelException(
                    "A execução de código está temporariamente indisponível. Tente novamente mais tarde.");
        }
        String saida = limitar(run.stdout());
        String erros = limitar(limparCaminhos(run.stderr()));
        String status = run.status() == null ? "" : run.status();

        SituacaoExecucao situacao = switch (status) {
            case "TO" -> SituacaoExecucao.TEMPO_ESGOTADO;
            case "OL", "EL" -> SituacaoExecucao.SAIDA_EXCEDIDA;
            case "SG" -> SituacaoExecucao.INTERROMPIDO;
            case "XX" -> throw new ExecutorIndisponivelException( // erro interno do Piston, não do aluno
                    "A execução de código está temporariamente indisponível. Tente novamente mais tarde.");
            default -> {
                if (run.code() != null && run.code() == 0) yield SituacaoExecucao.SUCESSO;
                yield erros.contains(MARCA_ERRO_COMPILACAO)
                        ? SituacaoExecucao.ERRO_COMPILACAO
                        : SituacaoExecucao.ERRO_EXECUCAO;
            }
        };
        return new ResultadoExecucao(situacao, saida, erros, run.code(), run.wallTime());
    }

    /**
     * O Piston grava o arquivo numa pasta temporária, e o erro aparece como
     * "/piston/jobs/3f2a.../Main.java:3: error: ...". O aluno só precisa ver "Main.java:3: error: ...".
     * (Também evita mostrar caminhos internos do servidor.)
     */
    static String limparCaminhos(String texto) {
        return texto == null ? "" : texto.replaceAll("(?:/[^\\s:/]+)+/" + ARQUIVO_FONTE.replace(".", "\\."), ARQUIVO_FONTE);
    }

    static String limitar(String texto) {
        if (texto == null) return "";
        return texto.length() <= LIMITE_TEXTO ? texto : texto.substring(0, LIMITE_TEXTO) + "\n[... saída cortada ...]";
    }
}
