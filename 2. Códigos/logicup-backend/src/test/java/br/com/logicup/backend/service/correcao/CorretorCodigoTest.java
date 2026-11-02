package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.dto.DetalheCorrecaoResponse;
import br.com.logicup.backend.exception.ExecutorIndisponivelException;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.execucao.ExecutorFalso;
import br.com.logicup.backend.execucao.ResultadoExecucao;
import br.com.logicup.backend.execucao.SituacaoExecucao;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.TipoExercicio;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 9 (ADR-001): testes de unidade do CorretorCodigo com o EXECUTOR FALSO.
 * O executor falso devolve a saída que o teste mandar, como se fosse o Piston: assim testamos
 * a LÓGICA da correção (casos, primeiro caso que falhou, pendente, erros) sem Docker.
 * (O teste com o Piston de verdade e as 9 questões reais é o QuestoesCodigoRealTest.)
 */
class CorretorCodigoTest {

    private static final String M = ComparadorSaida.MARCA_DE_CASO;

    // Questão pequena no formato real: 3 casos, comparação exata
    private static final String GABARITO = """
            {"palavrasChave": [["if"], ["else"]], "minimo": 2, "respostaModelo": "if (x > 0) ... else ...",
             "modo": "TRECHO", "comparacao": "EXATA",
             "programaTeste": "public class Main {\\n    static void executar(int x) {\\n//@CODIGO_DO_ALUNO\\n    }\\n}\\n",
             "programaExemplo": "public class Main {\\n    static void executar(int x) {\\n//@CODIGO_DO_ALUNO\\n    }\\n}\\n",
             "casos": [{"descricao": "x = 5", "saida": "positivo"},
                       {"descricao": "x = -1", "saida": "negativo"},
                       {"descricao": "x = 0", "saida": "negativo"}]}""";

    private final ExecutorFalso executor = new ExecutorFalso();
    private final CorretorCodigo corretor = new CorretorCodigo(JsonMapper.builder().build(), executor);
    private final Exercicio exercicio = new Exercicio(null, "T.Q1", "Teste", 1, TipoExercicio.ESCREVER_CODIGO,
            FormatoResposta.CODIGO, 25, "Enunciado", GABARITO, "Explicação");

    private void pistonDevolve(SituacaoExecucao situacao, String saida, String erros) {
        executor.quandoExecutar(p -> new ResultadoExecucao(situacao, saida, erros, 0, 700L));
    }

    private ResultadoCorrecao corrigir(String codigo) {
        return corretor.avaliar(exercicio, Map.of("codigo", codigo));
    }

    @Test
    void todosOsCasosCertosEhCorreta() {
        pistonDevolve(SituacaoExecucao.SUCESSO, M + "positivo\n" + M + "negativo\n" + M + "negativo\n", "");

        ResultadoCorrecao r = corrigir("if (x > 0) System.out.println(\"positivo\"); else System.out.println(\"negativo\");");

        assertTrue(r.correta());
        assertEquals("TESTES_OK", r.detalhe().tipo());
        assertEquals(3, r.detalhe().casosAprovados());
        // o código do aluno foi encaixado no programa de teste antes de ir para o executor
        String enviado = executor.recebidos().getFirst().codigoFonte();
        assertTrue(enviado.contains("static void executar(int x) {\nif (x > 0)"));
    }

    @Test
    void mostraSoOPrimeiroCasoQueFalhou() {
        pistonDevolve(SituacaoExecucao.SUCESSO, M + "positivo\n" + M + "negativo\n" + M + "positivo\n", "");

        ResultadoCorrecao r = corrigir("if (x >= 0) System.out.println(\"positivo\"); else System.out.println(\"negativo\");");

        assertFalse(r.correta());
        DetalheCorrecaoResponse d = r.detalhe();
        assertEquals("SAIDA_DIFERENTE", d.tipo());
        assertEquals(2, d.casosAprovados());
        assertEquals(3, d.totalCasos());
        assertEquals("x = 0", d.caso());
        assertEquals("negativo", d.esperado());
        assertEquals("positivo", d.obtido());
    }

    @Test
    void erroDeCompilacaoMostraALinhaDoAlunoEUmaDica() {
        // no programa montado, o código do aluno começa na linha 3 -> "Main.java:4" é a linha 2 do aluno
        pistonDevolve(SituacaoExecucao.ERRO_COMPILACAO, "", """
                Main.java:4: error: ';' expected
                1 error
                error: compilation failed""");

        ResultadoCorrecao r = corrigir("int y = 1;\nSystem.out.println(\"oi\")");

        assertFalse(r.correta());
        assertEquals("ERRO_COMPILACAO", r.detalhe().tipo());
        assertTrue(r.detalhe().erros().startsWith("linha 2: error: ';' expected"), r.detalhe().erros());
        assertTrue(r.detalhe().dica().contains("ponto e vírgula"));
    }

    @Test
    void excecaoNoMeioIndicaOCasoEmQueParou() {
        // passou no 1º caso e estourou no 2º
        pistonDevolve(SituacaoExecucao.ERRO_EXECUCAO, M + "positivo\n" + M,
                "Exception in thread \"main\" java.lang.ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3");

        ResultadoCorrecao r = corrigir("int[] v = new int[3]; System.out.println(v[3]);");

        assertEquals("ERRO_EXECUCAO", r.detalhe().tipo());
        assertEquals("x = -1", r.detalhe().caso());
        assertEquals(1, r.detalhe().casosAprovados());
        assertNotNull(r.detalhe().dica());
    }

    @Test
    void lacoInfinitoEhIncorretaComOCaso() {
        pistonDevolve(SituacaoExecucao.TEMPO_ESGOTADO, M, "");
        ResultadoCorrecao r = corrigir("while (true) { }");
        assertEquals("TEMPO_ESGOTADO", r.detalhe().tipo());
        assertEquals("x = 5", r.detalhe().caso());
    }

    @Test
    void programaQueParaAntesDoFimNaoEhCorreto() {
        pistonDevolve(SituacaoExecucao.SUCESSO, M + "positivo\n", ""); // ex.: System.exit(0) no 1º caso
        ResultadoCorrecao r = corrigir("System.out.println(\"positivo\"); System.exit(0);");
        assertFalse(r.correta());
        assertEquals("x = -1", r.detalhe().caso());
    }

    @Test
    void executorForaDoArDeixaPendenteComPreviaPorPalavrasChave() {
        executor.quandoExecutar(p -> { throw new ExecutorIndisponivelException("fora do ar"); });

        ResultadoCorrecao completo = corrigir("if (x > 0) System.out.println(\"positivo\"); else System.out.println(\"negativo\");");
        ResultadoCorrecao incompleto = corrigir("System.out.println(\"positivo\");");

        assertTrue(completo.pendente());
        assertFalse(completo.correta(), "pendente NUNCA conta como acerto");
        assertEquals("EXECUTOR_INDISPONIVEL", completo.detalhe().tipo());
        assertTrue(completo.detalhe().mensagem().contains("NÃO foi validado"));
        assertTrue(completo.detalhe().mensagem().contains("parece ter os elementos esperados"));
        assertTrue(incompleto.pendente());
        assertTrue(incompleto.detalhe().mensagem().contains("ainda falta algum elemento"));
    }

    @Test
    void programaInteiroNaQuestaoDeTrechoNemChegaAoExecutor() {
        ResultadoCorrecao r = corrigir("public class Main { public static void main(String[] a) { } }");
        assertFalse(r.correta());
        assertEquals("ESTRUTURA", r.detalhe().tipo());
        assertEquals(0, executor.recebidos().size(), "não gastou uma execução");
    }

    @Test
    void respostaVaziaOuGrandeDemaisEhRecusada() {
        assertThrows(RequisicaoInvalidaException.class, () -> corrigir("   "));
        assertThrows(RequisicaoInvalidaException.class, () -> corrigir("x".repeat(CorretorCodigo.TAMANHO_MAXIMO + 1)));
        assertThrows(RequisicaoInvalidaException.class, () -> corretor.avaliar(exercicio, Map.of("texto", "if")));
    }

    @Test
    void respostaCorretaMostraARespostaModeloENuncaOsCasos() {
        Object mostrado = corretor.respostaCorreta(exercicio);
        assertEquals("if (x > 0) ... else ...", mostrado);
    }

    @Test
    void gabaritoIncompletoEhErroNosso() {
        Exercicio quebrado = new Exercicio(null, "T.Q2", "Teste", 1, TipoExercicio.ESCREVER_CODIGO,
                FormatoResposta.CODIGO, 25, "Enunciado", "{\"respostaModelo\":\"x\"}", "Explicação");
        assertThrows(IllegalStateException.class, () -> corretor.avaliar(quebrado, Map.of("codigo", "x")));
    }
}
