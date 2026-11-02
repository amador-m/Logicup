package br.com.logicup.backend.execucao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 9 (ADR-001): testes contra o PISTON DE VERDADE (no Docker).
 *
 * Só rodam se a variável de ambiente PISTON_URL existir (ex.: PISTON_URL=http://localhost:2000).
 * Sem ela, o JUnit PULA esta classe: o "mvnw test" normal continua sem depender do Docker.
 *
 * Além de "funciona?", estes testes conferem a SEGURANÇA na prática: um programa malicioso
 * não acessa a internet, não consome memória sem limite e não roda para sempre.
 * Os limites usados são os mesmos do application.properties.
 */
@EnabledIfEnvironmentVariable(named = "PISTON_URL", matches = "https?://.+")
class ExecutorPistonRealTest {

    private final ExecutorPiston executor = new ExecutorPiston(new ParametrosExecutor(
            System.getenv("PISTON_URL"), "15.0.2", Duration.ofSeconds(6), Duration.ofSeconds(5),
            256L * 1024 * 1024, 3, Duration.ofSeconds(10)), JsonMapper.builder().build());

    private ResultadoExecucao rodar(String corpoDoMain) {
        return rodarArquivo("public class Main {\n    public static void main(String[] args) throws Exception {\n"
                + corpoDoMain + "\n    }\n}");
    }

    private ResultadoExecucao rodarArquivo(String codigo) {
        ResultadoExecucao r = executor.executar(ProgramaJava.semEntrada(codigo));
        System.out.printf("[Piston real] %s em %s ms%n", r.situacao(), r.tempoMs()); // ajuda a calibrar o tempo
        return r;
    }

    @Test
    void olaMundo() {
        ResultadoExecucao r = rodar("System.out.println(\"Olá, Gattuccino!\");");
        assertEquals(SituacaoExecucao.SUCESSO, r.situacao(), r.erros());
        assertEquals("Olá, Gattuccino!\n", r.saida());
    }

    /**
     * Encontrado no teste real (02/11): sem configuração, a sandbox usa ASCII e "café" vira "caf?".
     * Isso reprovaria alunos que acertaram (ex.: F4.Q9 "Exausto: hora do café!").
     * O código do caractere (233) mostra se o Java LEU o código certo; a última linha mostra se ele ESCREVEU certo.
     */
    @Test
    void acentosFuncionamNaLeituraENaSaida() {
        ResultadoExecucao r = rodar("""
                System.out.println(System.getProperty("file.encoding"));
                System.out.println((int) "é".charAt(0));
                System.out.println("Exausto: hora do café!");""");
        String[] linhas = r.saida().split("\n");
        assertEquals(3, linhas.length, "saída: " + r.saida() + r.erros());
        assertEquals("233", linhas[1], "o Java LEU o código com a codificação errada (file.encoding=" + linhas[0] + ")");
        assertEquals("Exausto: hora do café!", linhas[2], "o Java ESCREVEU com a codificação errada (file.encoding=" + linhas[0] + ")");
    }

    @Test
    void leAEntradaDoTeclado() {
        ResultadoExecucao r = executor.executar(new ProgramaJava("""
                import java.util.Scanner;
                public class Main {
                    public static void main(String[] args) {
                        Scanner teclado = new Scanner(System.in);
                        int energia = teclado.nextInt();
                        System.out.println("Energia: " + energia);
                    }
                }""", "7\n"));
        assertEquals("Energia: 7\n", r.saida(), r.erros());
    }

    @Test
    void variasClassesNoMesmoArquivoComoNasQuestoesDeClasse() {
        ResultadoExecucao r = rodarArquivo("""
                public class Main {
                    public static void main(String[] args) {
                        System.out.println(new Bau(30).getGemas());
                    }
                }
                public class Bau {
                    private int gemas;
                    public Bau(int gemas) { this.gemas = gemas; }
                    public int getGemas() { return gemas; }
                }""");
        assertEquals("30\n", r.saida(), r.erros());
    }

    @Test
    void erroDeCompilacao() {
        ResultadoExecucao r = rodar("System.out.println(\"oi\")");
        assertEquals(SituacaoExecucao.ERRO_COMPILACAO, r.situacao());
        assertTrue(r.erros().contains("Main.java:3"), r.erros());
        assertFalse(r.erros().contains("/piston/"), r.erros());
    }

    @Test
    void excecao() {
        assertEquals(SituacaoExecucao.ERRO_EXECUCAO, rodar("int[] v = new int[2]; System.out.println(v[5]);").situacao());
    }

    // ---------------- segurança na prática ----------------

    @Test
    void lacoInfinitoEhInterrompido() {
        assertEquals(SituacaoExecucao.TEMPO_ESGOTADO, rodar("while (true) { }").situacao());
    }

    @Test
    void naoAcessaAInternet() {
        ResultadoExecucao r = rodar("""
                try (java.net.Socket s = new java.net.Socket()) {
                    s.connect(new java.net.InetSocketAddress("1.1.1.1", 80), 2000);
                    System.out.println("CONECTOU");
                } catch (Exception e) {
                    System.out.println("BLOQUEADO");
                }""");
        assertEquals("BLOQUEADO\n", r.saida(), "a sandbox NÃO pode ter rede: " + r.erros());
    }

    @Test
    void naoConsomeMemoriaSemLimite() {
        ResultadoExecucao r = rodar("""
                java.util.List<byte[]> lista = new java.util.ArrayList<>();
                for (int i = 0; i < 2000; i++) { lista.add(new byte[1024 * 1024]); } // tenta 2 GB
                System.out.println("ALOCOU TUDO");""");
        assertNotEquals("ALOCOU TUDO\n", r.saida(), "o limite de memória não funcionou");
        assertFalse(r.sucesso());
    }

    @Test
    void saidaInfinitaEhCortada() {
        ResultadoExecucao r = rodar("while (true) { System.out.println(\"spam\"); }");
        assertTrue(r.situacao() == SituacaoExecucao.SAIDA_EXCEDIDA || r.situacao() == SituacaoExecucao.TEMPO_ESGOTADO,
                r.situacao().toString());
        assertTrue(r.saida().length() <= ExecutorPiston.LIMITE_TEXTO + 50);
    }
}
