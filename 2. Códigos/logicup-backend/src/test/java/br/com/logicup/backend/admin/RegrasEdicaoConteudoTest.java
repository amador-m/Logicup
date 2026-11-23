package br.com.logicup.backend.admin;

import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

/** Sprint 11: a edição de conteúdo só pode trocar TEXTOS, nunca a estrutura. */
class RegrasEdicaoConteudoTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final String ATUAL = """
            {"objetivos": ["Entender algoritmos."],
             "teoria": [{"titulo": "O que é", "texto": "velho",
                         "blocos": [{"tipo": "paragrafo", "texto": "Um algoritmo é..."},
                                    {"tipo": "lista", "itens": ["um", "dois"]},
                                    {"tipo": "codigo", "codigo": "int x = 1;"},
                                    {"tipo": "tabela", "linhas": [["", "A"], ["1", "B"]]}]}],
             "exemplos": []}
            """;

    private static JsonNode ler(String json) {
        return JSON.readTree(json);
    }

    private static String trocar(String de, String para) {
        assertTrue(ATUAL.contains(de), "o teste precisa trocar algo que existe: " + de);
        return ATUAL.replace(de, para);
    }

    @Test
    void trocarSoTextosEhPermitido() {
        JsonNode novo = ler(trocar("Um algoritmo é...", "Um algoritmo é uma receita.")
                .replace("\"dois\"", "\"três\"").replace("int x = 1;", "int x = 2;"));
        assertDoesNotThrow(() -> RegrasEdicaoConteudo.exigirMesmaForma(ler(ATUAL), novo, "conteudo"));
        assertDoesNotThrow(() -> RegrasEdicaoConteudo.exigirTextosPreenchidos(novo, "conteudo"));
    }

    @Test
    void naoPodeRemoverBloco() {
        JsonNode novo = ler(trocar("{\"tipo\": \"codigo\", \"codigo\": \"int x = 1;\"},", ""));
        RequisicaoInvalidaException erro = assertThrows(RequisicaoInvalidaException.class,
                () -> RegrasEdicaoConteudo.exigirMesmaForma(ler(ATUAL), novo, "conteudo"));
        assertTrue(erro.getMessage().contains("conteudo.teoria[0].blocos"), erro.getMessage());
    }

    @Test
    void naoPodeTrocarOTipoDoBloco() {
        JsonNode novo = ler(trocar("\"tipo\": \"paragrafo\"", "\"tipo\": \"codigo\""));
        assertThrows(RequisicaoInvalidaException.class,
                () -> RegrasEdicaoConteudo.exigirMesmaForma(ler(ATUAL), novo, "conteudo"));
    }

    @Test
    void naoPodeAcrescentarCampo() {
        JsonNode novo = ler(trocar("\"exemplos\": []", "\"exemplos\": [], \"extra\": 1"));
        assertThrows(RequisicaoInvalidaException.class,
                () -> RegrasEdicaoConteudo.exigirMesmaForma(ler(ATUAL), novo, "conteudo"));
    }

    @Test
    void textoNaoPodeFicarVazioMasCelulaDeTabelaPode() {
        JsonNode vazio = ler(trocar("Um algoritmo é...", "   "));
        assertThrows(RequisicaoInvalidaException.class,
                () -> RegrasEdicaoConteudo.exigirTextosPreenchidos(vazio, "conteudo"));
        // a célula "" do cabeçalho da tabela já vem vazia e continua aceita
        assertDoesNotThrow(() -> RegrasEdicaoConteudo.exigirTextosPreenchidos(ler(ATUAL), "conteudo"));
    }

    @Test
    void textoGrandeDemaisEhRecusado() {
        JsonNode novo = ler(trocar("Um algoritmo é...", "x".repeat(RegrasEdicaoConteudo.TAMANHO_MAXIMO_TEXTO + 1)));
        assertThrows(RequisicaoInvalidaException.class,
                () -> RegrasEdicaoConteudo.exigirMesmaForma(ler(ATUAL), novo, "conteudo"));
    }

    @Test
    void textoDaSecaoEhRecalculadoComARegraDoGerador() {
        JsonNode conteudo = ler(ATUAL);
        RegrasEdicaoConteudo.recalcularTextoDasSecoes(conteudo);
        assertEquals("Um algoritmo é...\n- um\n- dois\nint x = 1;\n | A\n1 | B",
                conteudo.get("teoria").get(0).get("texto").asString());
    }
}
