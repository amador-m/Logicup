package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.execucao.ExecutorPiston;
import br.com.logicup.backend.execucao.ParametrosExecutor;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.TipoExercicio;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 9 (ADR-001): as 9 questões REAIS de código, corrigidas pelo CorretorCodigo no PISTON DE VERDADE.
 *
 * Só roda com a variável PISTON_URL (igual ao ExecutorPistonRealTest). Para cada questão:
 *   - a solução de referência precisa dar CORRETA;
 *   - um erro típico precisa dar INCORRETA.
 * Isso prova que os programas de teste e as saídas esperadas (gerados pelo gerar_questoes_codigo.py com o
 * Java do computador) funcionam IGUAL no Java 15 do Piston, inclusive com acentos.
 *
 * @TestFactory cria um teste por questão (aparecem separados no IntelliJ: "F4.Q9 referência", "F4.Q9 erro"...).
 */
@EnabledIfEnvironmentVariable(named = "PISTON_URL", matches = "https?://.+")
class QuestoesCodigoRealTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private final CorretorCodigo corretor = new CorretorCodigo(jsonMapper, new ExecutorPiston(new ParametrosExecutor(
            System.getenv("PISTON_URL"), "15.0.2", Duration.ofSeconds(6), Duration.ofSeconds(5),
            256L * 1024 * 1024, 3, Duration.ofSeconds(10)), jsonMapper));

    @TestFactory
    List<DynamicTest> asNoveQuestoesNoPiston() throws Exception {
        JsonNode questoes;
        try (InputStream arquivo = getClass().getResourceAsStream("/questoes/questoes-codigo-teste.json")) {
            assertNotNull(arquivo, "falta src/test/resources/questoes/questoes-codigo-teste.json");
            questoes = jsonMapper.readTree(arquivo);
        }
        List<DynamicTest> testes = new ArrayList<>();
        for (Map.Entry<String, JsonNode> questao : questoes.properties()) {
            String codigo = questao.getKey();
            Exercicio exercicio = exercicio(codigo, questao.getValue().get("gabarito"));
            String referencia = questao.getValue().get("referencia").asString();
            String erro = questao.getValue().get("erro").get("codigo").asString();
            String nomeDoErro = questao.getValue().get("erro").get("nome").asString();

            testes.add(DynamicTest.dynamicTest(codigo + " — referência passa", () -> {
                ResultadoCorrecao r = corretor.avaliar(exercicio, Map.of("codigo", referencia));
                assertTrue(r.correta(), codigo + ": " + r.detalhe());
            }));
            testes.add(DynamicTest.dynamicTest(codigo + " — erro \"" + nomeDoErro + "\" reprova", () -> {
                ResultadoCorrecao r = corretor.avaliar(exercicio, Map.of("codigo", erro));
                assertFalse(r.correta(), codigo + ": o erro passou");
                assertFalse(r.pendente(), codigo + ": o Piston não respondeu");
                System.out.printf("[%s] %s -> %s (%s)%n", codigo, nomeDoErro, r.detalhe().tipo(), r.detalhe().caso());
            }));
        }
        return testes;
    }

    /** Monta a questão como estará no banco (as palavras-chave de reserva não importam aqui). */
    private Exercicio exercicio(String codigo, JsonNode gabaritoGerado) {
        ObjectNode gabarito = (ObjectNode) gabaritoGerado.deepCopy();
        gabarito.set("palavrasChave", jsonMapper.readTree("[[\"x\"]]"));
        gabarito.put("minimo", 1);
        gabarito.put("respostaModelo", "(modelo)");
        return new Exercicio(null, codigo, codigo, 1, TipoExercicio.ESCREVER_CODIGO, FormatoResposta.CODIGO, 25,
                "Enunciado", jsonMapper.writeValueAsString(gabarito), "Explicação");
    }
}
