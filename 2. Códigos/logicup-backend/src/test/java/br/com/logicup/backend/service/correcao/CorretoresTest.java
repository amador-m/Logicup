package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.TipoExercicio;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes de UNIDADE dos corretores (padrão Strategy): sem Spring, sem banco.
 * As respostas do aluno são Map, exatamente como o Spring entrega o JSON recebido.
 */
class CorretoresTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    private static Exercicio exercicio(FormatoResposta formato, String alternativas, String gabarito) {
        Exercicio e = new Exercicio(null, "TESTE.Q1", "Questão", 1, TipoExercicio.MULTIPLA_ESCOLHA,
                formato, 10, "Enunciado", gabarito, "Explicação");
        e.setAlternativasJson(alternativas);
        return e;
    }

    // ---------------- ESCOLHA_UNICA ----------------
    private final CorretorEscolhaUnica escolha = new CorretorEscolhaUnica(mapper);
    private final Exercicio exEscolha = exercicio(FormatoResposta.ESCOLHA_UNICA,
            "[{\"id\":\"a\",\"texto\":\"Verdadeiro\"},{\"id\":\"b\",\"texto\":\"Falso\"}]",
            "{\"correta\":\"a\"}");

    @Test
    void escolhaUnicaAcertaEErra() {
        assertTrue(escolha.corrigir(exEscolha, Map.of("alternativa", "a")));
        assertFalse(escolha.corrigir(exEscolha, Map.of("alternativa", "b")));
    }

    @Test
    void escolhaUnicaMostraAAlternativaCerta() {
        assertEquals(new Alternativa("a", "Verdadeiro"), escolha.respostaCorreta(exEscolha));
    }

    @Test
    void escolhaUnicaSemAlternativaEhRequisicaoInvalida() {
        assertThrows(RequisicaoInvalidaException.class, () -> escolha.corrigir(exEscolha, Map.of("texto", "a")));
    }

    // ---------------- ORDENACAO ----------------
    private final CorretorOrdenacao ordenacao = new CorretorOrdenacao(mapper);
    private final Exercicio exOrdem = exercicio(FormatoResposta.ORDENACAO,
            "[{\"id\":\"a\",\"texto\":\"Servir\"},{\"id\":\"b\",\"texto\":\"Adicionar pó\"},"
                    + "{\"id\":\"c\",\"texto\":\"Ferver a água\"},{\"id\":\"d\",\"texto\":\"Coar\"}]",
            "{\"ordem\":[\"c\",\"b\",\"d\",\"a\"]}");

    @Test
    void ordenacaoExigeAMesmaSequencia() {
        assertTrue(ordenacao.corrigir(exOrdem, Map.of("ordem", List.of("c", "b", "d", "a"))));
        assertFalse(ordenacao.corrigir(exOrdem, Map.of("ordem", List.of("b", "c", "d", "a"))));
    }

    @Test
    void ordenacaoMostraOsItensNaOrdemCerta() {
        List<?> certa = (List<?>) ordenacao.respostaCorreta(exOrdem);
        assertEquals(new Alternativa("c", "Ferver a água"), certa.get(0));
        assertEquals(new Alternativa("a", "Servir"), certa.get(3));
    }

    // Sprint 9 (C2): itens com o MESMO texto (as duas "}" da F2.Q4) são intercambiáveis
    private final Exercicio exOrdemChaves = exercicio(FormatoResposta.ORDENACAO,
            "[{\"id\":\"a\",\"texto\":\"}\"},{\"id\":\"b\",\"texto\":\"public static void main(String[] args) {\"},"
                    + "{\"id\":\"c\",\"texto\":\"public class Main {\"},{\"id\":\"d\",\"texto\":\"}\"}]",
            "{\"ordem\":[\"c\",\"b\",\"a\",\"d\"]}");

    @Test
    void ordenacaoAceitaItensDeTextoIgualEmQualquerOrdem() {
        assertTrue(ordenacao.corrigir(exOrdemChaves, Map.of("ordem", List.of("c", "b", "a", "d")))); // igual ao gabarito
        assertTrue(ordenacao.corrigir(exOrdemChaves, Map.of("ordem", List.of("c", "b", "d", "a")))); // "}" trocadas
        assertFalse(ordenacao.corrigir(exOrdemChaves, Map.of("ordem", List.of("b", "c", "a", "d")))); // ordem errada
    }

    @Test
    void ordenacaoRecusaRespostaQueNaoUsaCadaItemUmaVez() {
        // repetir a mesma "}" daria os mesmos TEXTOS do gabarito: precisa ser recusado
        assertFalse(ordenacao.corrigir(exOrdemChaves, Map.of("ordem", List.of("c", "b", "a", "a"))));
        assertFalse(ordenacao.corrigir(exOrdemChaves, Map.of("ordem", List.of("c", "b", "a"))));           // faltou item
        assertFalse(ordenacao.corrigir(exOrdemChaves, Map.of("ordem", List.of("c", "b", "a", "d", "x")))); // item a mais
    }

    // ---------------- ASSOCIACAO ----------------
    private final CorretorAssociacao associacao = new CorretorAssociacao(mapper);
    private final Exercicio exAssoc = exercicio(FormatoResposta.ASSOCIACAO,
            "{\"itens\":[{\"id\":\"1\",\"texto\":\"Dividir a festa\"},{\"id\":\"2\",\"texto\":\"Vitaminas parecidas\"}],"
                    + "\"opcoes\":[{\"id\":\"a\",\"texto\":\"Decomposição\"},{\"id\":\"b\",\"texto\":\"Padrões\"},"
                    + "{\"id\":\"c\",\"texto\":\"Algoritmos\"}]}",
            "{\"pares\":{\"1\":\"a\",\"2\":\"b\"}}");

    @Test
    void associacaoExigeTodosOsPares() {
        assertTrue(associacao.corrigir(exAssoc, Map.of("pares", Map.of("2", "b", "1", "a")))); // ordem não importa
        assertFalse(associacao.corrigir(exAssoc, Map.of("pares", Map.of("1", "a", "2", "c"))));
        assertFalse(associacao.corrigir(exAssoc, Map.of("pares", Map.of("1", "a"))));          // faltou par
    }

    @Test
    void associacaoMostraOsParesComTexto() {
        List<?> pares = (List<?>) associacao.respostaCorreta(exAssoc);
        assertTrue(pares.contains(new CorretorAssociacao.ParCorreto("Dividir a festa", "Decomposição")));
    }

    // ---------------- RESPOSTA_CURTA ----------------
    private final CorretorRespostaCurta curta = new CorretorRespostaCurta(mapper);

    @Test
    void respostaCurtaSemDiferenciarMaiusculas() {
        Exercicio ex = exercicio(FormatoResposta.RESPOSTA_CURTA, null,
                "{\"aceitas\":[\"Excluir\"],\"diferenciarMaiusculas\":false}");
        assertTrue(curta.corrigir(ex, Map.of("texto", " excluir. ")));
        assertFalse(curta.corrigir(ex, Map.of("texto", "Incluir")));
        assertFalse(curta.corrigir(ex, Map.of("texto", "   ")));
        assertEquals("Excluir", curta.respostaCorreta(ex));
    }

    @Test
    void respostaCurtaDiferenciandoMaiusculas() {
        Exercicio ex = exercicio(FormatoResposta.RESPOSTA_CURTA, null,
                "{\"aceitas\":[\"System.out.println\"],\"diferenciarMaiusculas\":true}");
        assertTrue(curta.corrigir(ex, Map.of("texto", "System.out.println")));
        assertFalse(curta.corrigir(ex, Map.of("texto", "system.out.println")));
    }

    @Test
    void respostaCurtaAceitaVariasFormas() {
        Exercicio ex = exercicio(FormatoResposta.RESPOSTA_CURTA, null, "{\"aceitas\":[\"8,5\",\"8.5\"]}");
        assertTrue(curta.corrigir(ex, Map.of("texto", "8.5")));
        assertTrue(curta.corrigir(ex, Map.of("texto", "8,5")));
    }

    // Sprint 9 (C1): várias lacunas — gabarito real da F3.Q3 ("___ idade = ___;")
    private final Exercicio f3q3 = exercicio(FormatoResposta.RESPOSTA_CURTA, "{\"lacunas\":2}",
            "{\"lacunas\":[{\"aceitas\":[\"int\"]},{\"aceitas\":[\"18\"]}],\"diferenciarMaiusculas\":true}");

    @Test
    void respostaCurtaComDuasLacunasExigeAsDuasCertas() {
        assertTrue(curta.corrigir(f3q3, Map.of("lacunas", List.of("int", "18"))));
        assertTrue(curta.corrigir(f3q3, Map.of("lacunas", List.of(" int ", "18.")))); // mesma limpeza da lacuna única
        assertFalse(curta.corrigir(f3q3, Map.of("lacunas", List.of("int", "17"))));   // 2ª errada
        assertFalse(curta.corrigir(f3q3, Map.of("lacunas", List.of("Int", "18"))));   // Java diferencia maiúsculas
        assertFalse(curta.corrigir(f3q3, Map.of("lacunas", List.of("18", "int"))));   // ordem das lacunas importa
        assertFalse(curta.corrigir(f3q3, Map.of("lacunas", List.of("int", "  "))));   // lacuna em branco
    }

    @Test
    void respostaCurtaComLacunasRecusaQuantidadeErrada() {
        assertThrows(RequisicaoInvalidaException.class,
                () -> curta.corrigir(f3q3, Map.of("lacunas", List.of("int"))));
        assertThrows(RequisicaoInvalidaException.class,
                () -> curta.corrigir(f3q3, Map.of("texto", "int idade = 18;"))); // formato de lacuna única
    }

    @Test
    void respostaCurtaComLacunasMostraUmaRespostaPorLacuna() {
        assertEquals(List.of("int", "18"), curta.respostaCorreta(f3q3));
    }

    // ---------------- TEXTO_LIVRE (gabaritos reais do seed da Fase 1) ----------------
    private final CorretorTextoLivre textoLivre = new CorretorTextoLivre(mapper);
    private final Exercicio f1q6 = exercicio(FormatoResposta.TEXTO_LIVRE, null,
            "{\"palavrasChave\":[[\"lig\"],[\"carro\",\"veícul\",\"motor\"]],\"minimo\":2,"
                    + "\"respostaModelo\":\"\\\"Ligar o carro\\\" deveria ser o primeiro passo.\"}");
    private final Exercicio f1q10 = exercicio(FormatoResposta.TEXTO_LIVRE, null,
            "{\"palavrasChave\":[[\"faixa\",\"pedestre\"],[\"olh\",\"observ\"],[\"veícul\",\"carro\",\"trânsito\"],"
                    + "[\"aguard\",\"esper\"],[\"atravess\"]],\"minimo\":3,\"respostaModelo\":\"1) Chegar até a faixa...\"}");

    private boolean texto(Exercicio ex, String resposta) {
        return textoLivre.corrigir(ex, Map.of("texto", resposta));
    }

    @Test
    void textoLivreF1Q6() {
        assertTrue(texto(f1q6, "Ligar o carro"));
        assertTrue(texto(f1q6, "ligar o carro deveria ser o primeiro passo"));
        assertTrue(texto(f1q6, "O carro precisa ser ligado primeiro"));
        assertTrue(texto(f1q6, "Primeiro liga o motor"));
        assertFalse(texto(f1q6, "Dirigir"));
        assertFalse(texto(f1q6, "estacionar o carro"));
    }

    @Test
    void textoLivreF1Q10ComSinonimosEConjugacoes() {
        assertTrue(texto(f1q10, "1) ir ate a faixa 2) olhar pros lados 3) esperar os carros passarem 4) atravessar"));
        assertTrue(texto(f1q10, "Olhe para os dois lados e atravesse quando não vier VEÍCULO"));
        assertTrue(texto(f1q10, "Observo o trânsito, espero e atravesso"));
        assertFalse(texto(f1q10, "atravessar a rua correndo"));
        assertFalse(texto(f1q10, "sei lá"));
    }

    // Sprint 9 (C3): o termo precisa começar uma palavra
    @Test
    void textoLivreNaoEncontraTermoNoMeioDeOutraPalavra() {
        Exercicio ex = exercicio(FormatoResposta.TEXTO_LIVRE, null,
                "{\"palavrasChave\":[[\"certo\"]],\"minimo\":1,\"respostaModelo\":\"Está certo\"}");
        assertTrue(texto(ex, "O resultado está certo"));
        assertTrue(texto(ex, "(certo)"));
        assertFalse(texto(ex, "O resultado é incerto")); // antes da Sprint 9 isto contava como acerto
    }

    @Test
    void textoLivreComTermoDeSimboloValeEmQualquerPosicao() {
        // critério real da F4.Q9: ["== 0", "==0"] — "energia==0" não tem espaço antes do "=="
        Exercicio ex = exercicio(FormatoResposta.TEXTO_LIVRE, null,
                "{\"palavrasChave\":[[\"== 0\",\"==0\"],[\"else if\"]],\"minimo\":2,\"respostaModelo\":\"...\"}");
        assertTrue(texto(ex, "if (energia==0) { } else if (energia < 5) { }"));
        assertFalse(texto(ex, "if (energia==0) { } elseif (x) { }")); // "else if" escrito junto não vale
    }

    @Test
    void textoLivreMostraSoARespostaModelo() {
        Object certa = textoLivre.respostaCorreta(f1q6);
        assertEquals("\"Ligar o carro\" deveria ser o primeiro passo.", certa);
        assertFalse(certa.toString().contains("veícul")); // as palavras-chave nunca aparecem
    }
}
