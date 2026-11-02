package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.service.correcao.ComparadorSaida.Comparacao;
import br.com.logicup.backend.service.correcao.MontadorPrograma.Modo;
import br.com.logicup.backend.service.correcao.MontadorPrograma.ProgramaMontado;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 9 (ADR-001): testes de unidade do MontadorPrograma e do ComparadorSaida (Java puro, sem Spring).
 * As regras precisam ser IGUAIS às do gerar_questoes_codigo.py, que gerou as saídas esperadas.
 */
class MontadorComparadorTest {

    private static final String PROGRAMA = """
            import java.util.*;

            public class Main {
                static void executar(int energia) {
            //@CODIGO_DO_ALUNO
                }
            }""";

    // ---------------- MontadorPrograma ----------------

    @Test
    void colaOCodigoDoAlunoNoLugarDoMarcador() {
        ProgramaMontado p = MontadorPrograma.montar(PROGRAMA, "System.out.println(energia);");
        assertTrue(p.codigo().contains("static void executar(int energia) {\nSystem.out.println(energia);\n    }"));
        assertFalse(p.codigo().contains(MontadorPrograma.PONTO_DE_ENCAIXE));
        assertEquals(5, p.primeiraLinhaDoAluno()); // a linha do marcador no programa (contando de 1)
    }

    @Test
    void importDoAlunoSobeParaOTopoSemMudarANumeracao() {
        ProgramaMontado p = MontadorPrograma.montar(PROGRAMA, "import java.util.ArrayList;\nint x = 1;");
        assertTrue(p.codigo().startsWith("import java.util.ArrayList;\n"));
        // o import virou linha vazia: "int x = 1;" continua sendo a linha 2 DO ALUNO
        assertEquals("linha 2", p.traduzirLinhas("Main.java:" + (p.primeiraLinhaDoAluno() + 1)));
    }

    @Test
    void packageDoAlunoEhRemovido() {
        ProgramaMontado p = MontadorPrograma.montar(PROGRAMA, "package minhas.coisas;\nint x = 1;");
        assertFalse(p.codigo().contains("package"));
    }

    @Test
    void traduzAsLinhasDoErroParaAsLinhasDoAluno() {
        ProgramaMontado p = MontadorPrograma.montar(PROGRAMA, "int a = 1;\nint b = 2\nint c = 3;");
        int linhaDoErroNoPrograma = p.primeiraLinhaDoAluno() + 1; // 2ª linha do aluno
        assertEquals("linha 2: error: ';' expected",
                p.traduzirLinhas("Main.java:" + linhaDoErroNoPrograma + ": error: ';' expected"));
        // erro fora do código do aluno (no nosso programa de teste)
        assertEquals("código de teste da questão: error: x", p.traduzirLinhas("Main.java:1: error: x"));
        // pilha de exceção: "(Main.java:N)"
        assertEquals("at Main.executar(linha 3)", p.traduzirLinhas("at Main.executar(Main.java:" + (linhaDoErroNoPrograma + 1) + ")"));
    }

    @Test
    void programaDeTesteSemMarcadorEhErroNosso() {
        assertThrows(IllegalStateException.class, () -> MontadorPrograma.montar("public class Main {}", "x"));
    }

    @Test
    void recusaProgramaInteiroQuandoAQuestaoPedeTrechoOuMetodo() {
        String programaInteiro = "public class Main { public static void main(String[] args) { } }";
        assertThrows(RequisicaoInvalidaException.class, () -> MontadorPrograma.conferirEstrutura(Modo.TRECHO, programaInteiro));
        assertThrows(RequisicaoInvalidaException.class, () -> MontadorPrograma.conferirEstrutura(Modo.MEMBRO, programaInteiro));
        assertThrows(RequisicaoInvalidaException.class, () -> MontadorPrograma.conferirEstrutura(Modo.CLASSES, programaInteiro));
        // nas questões de classe, um main DENTRO da classe do aluno não atrapalha
        assertDoesNotThrow(() -> MontadorPrograma.conferirEstrutura(Modo.CLASSES,
                "class Bau { public static void main(String[] a) {} }"));
        assertDoesNotThrow(() -> MontadorPrograma.conferirEstrutura(Modo.TRECHO, "System.out.println(1);"));
    }

    // ---------------- ComparadorSaida ----------------

    @Test
    void comparacaoExataIgnoraDiferencasInvisiveis() {
        assertTrue(ComparadorSaida.iguais(Comparacao.EXATA, "Energia baixa", "Energia baixa   \r\n\n"));
        assertFalse(ComparadorSaida.iguais(Comparacao.EXATA, "Energia baixa", "energia baixa"), "maiúsculas contam");
        assertFalse(ComparadorSaida.iguais(Comparacao.EXATA, "café", "cafe"), "acento conta");
    }

    @Test
    void comparacaoPorNumerosAceitaTextoEmVolta() {
        assertTrue(ComparadorSaida.iguais(Comparacao.NUMEROS, "2\n4\n6", "Missão 2\nMissão 4\nMissão 6"));
        assertFalse(ComparadorSaida.iguais(Comparacao.NUMEROS, "2\n4\n6", "2\n4"));
    }

    @Test
    void comparacaoPeloUltimoNumero() {
        assertTrue(ComparadorSaida.iguais(Comparacao.ULTIMO_NUMERO, "3", "Missões com nota alta: 3"));
        assertFalse(ComparadorSaida.iguais(Comparacao.ULTIMO_NUMERO, "0", ""), "não exibir nada não vale como 0");
        assertTrue(ComparadorSaida.iguais(Comparacao.ULTIMO_NUMERO, "-2", "maior: -2"));
    }

    @Test
    void separaASaidaPorCaso() {
        String m = ComparadorSaida.MARCA_DE_CASO;
        assertEquals(List.of("a\n", "b\n", ""), ComparadorSaida.separarCasos("lixo" + m + "a\n" + m + "b\n" + m));
        assertEquals(List.of(), ComparadorSaida.separarCasos("nenhuma marca"));
        // aluno usou print (sem quebra de linha): a marca não precisa estar no começo da linha
        assertEquals(List.of("Energia baixa", "Pronto"), ComparadorSaida.separarCasos(m + "Energia baixa" + m + "Pronto"));
    }

    @Test
    void dicasParaErrosComuns() {
        assertNotNull(DicasDeErro.para("Main.java:5: error: variable energia is already defined in method executar(int)"));
        assertTrue(DicasDeErro.para("java.lang.StackOverflowError").contains("super"));
        assertNull(DicasDeErro.para("um erro que ninguém conhece"));
        assertNull(DicasDeErro.para(""));
    }
}
