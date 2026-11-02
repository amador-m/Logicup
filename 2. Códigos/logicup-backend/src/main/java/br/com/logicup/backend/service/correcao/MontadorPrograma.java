package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.exception.RequisicaoInvalidaException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sprint 9 (ADR-001): encaixa o código do aluno no PROGRAMA DE TESTE da questão.
 *
 * Por que isso existe: as questões pedem um trecho, um método ou uma classe, e não um programa
 * completo. O programa de teste (guardado no gabarito, secreto) tem um "buraco" marcado com
 * //@CODIGO_DO_ALUNO; aqui colamos o código do aluno nesse buraco.
 *
 * Regras (as MESMAS do gerador gerar_questoes_codigo.py, que produziu as saídas esperadas):
 *  - linhas "import ...;" do aluno sobem para o topo do arquivo (import no meio de uma classe não compila).
 *    No lugar delas fica uma linha vazia, para a numeração das linhas do aluno não mudar;
 *  - linhas "package ...;" são removidas (no modo arquivo único, elas quebram a execução);
 *  - o resto do código é colado como está.
 *
 * Classe sem Spring e sem estado: só regras, testadas por testes de unidade simples.
 */
public final class MontadorPrograma {

    public static final String PONTO_DE_ENCAIXE = "//@CODIGO_DO_ALUNO";

    private static final Pattern IMPORT = Pattern.compile("^\\s*import\\s+(static\\s+)?[\\w.]+(\\.\\*)?\\s*;\\s*$");
    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+[\\w.]+\\s*;\\s*$");
    private static final Pattern CLASSE_MAIN = Pattern.compile("\\bclass\\s+Main\\b");
    private static final Pattern METODO_MAIN = Pattern.compile("\\bstatic\\s+void\\s+main\\s*\\(");
    private static final Pattern LINHA_DO_ARQUIVO = Pattern.compile("Main\\.java:(\\d+)");

    /** Onde o código do aluno entra (ver questoes_codigo.py). */
    public enum Modo { TRECHO, MEMBRO, CLASSES }

    /**
     * O programa pronto para executar + onde está o código do aluno nele.
     * Serve para traduzir "Main.java:12" (linha do programa inteiro) para "linha 3" (linha do aluno).
     */
    public record ProgramaMontado(String codigo, int primeiraLinhaDoAluno, int linhasDoAluno) {

        /** Troca as linhas do arquivo montado pelas linhas que o ALUNO escreveu. */
        public String traduzirLinhas(String mensagem) {
            if (mensagem == null || mensagem.isEmpty()) return "";
            Matcher m = LINHA_DO_ARQUIVO.matcher(mensagem);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                int linhaNoPrograma = Integer.parseInt(m.group(1));
                int linhaDoAluno = linhaNoPrograma - primeiraLinhaDoAluno + 1;
                String troca = (linhaDoAluno >= 1 && linhaDoAluno <= linhasDoAluno)
                        ? "linha " + linhaDoAluno
                        : "código de teste da questão";
                m.appendReplacement(sb, Matcher.quoteReplacement(troca));
            }
            m.appendTail(sb);
            return sb.toString();
        }
    }

    private MontadorPrograma() {}

    public static ProgramaMontado montar(String programaDeTeste, String codigoDoAluno) {
        String[] linhasDoPrograma = programaDeTeste.replace("\r\n", "\n").split("\n", -1);
        int posicao = -1;
        for (int i = 0; i < linhasDoPrograma.length; i++) {
            if (linhasDoPrograma[i].trim().equals(PONTO_DE_ENCAIXE)) {
                if (posicao >= 0) throw new IllegalStateException("O programa de teste tem mais de um " + PONTO_DE_ENCAIXE);
                posicao = i;
            }
        }
        if (posicao < 0) throw new IllegalStateException("O programa de teste não tem o " + PONTO_DE_ENCAIXE);

        List<String> imports = new ArrayList<>();
        List<String> linhasDoAluno = new ArrayList<>();
        for (String linha : codigoDoAluno.replace("\r\n", "\n").split("\n", -1)) {
            if (IMPORT.matcher(linha).matches()) {
                imports.add(linha.trim());
                linhasDoAluno.add("");
            } else if (PACKAGE.matcher(linha).matches()) {
                linhasDoAluno.add("");
            } else {
                linhasDoAluno.add(linha);
            }
        }

        List<String> resultado = new ArrayList<>(imports);
        for (int i = 0; i < linhasDoPrograma.length; i++) {
            if (i == posicao) resultado.addAll(linhasDoAluno);
            else resultado.add(linhasDoPrograma[i]);
        }
        // +1 porque as linhas do compilador começam em 1
        int primeiraLinhaDoAluno = imports.size() + posicao + 1;
        return new ProgramaMontado(String.join("\n", resultado), primeiraLinhaDoAluno, linhasDoAluno.size());
    }

    /**
     * Confere se o aluno escreveu SÓ o que a questão pede. Um erro comum é colar um programa inteiro
     * ("public class Main { public static void main...") quando a questão pede só um trecho ou método;
     * o compilador daria um erro confuso, então explicamos antes, em português.
     *
     * @throws RequisicaoInvalidaException com a explicação (o CorretorCodigo transforma em resposta INCORRETA)
     */
    public static void conferirEstrutura(Modo modo, String codigoDoAluno) {
        boolean temClasseMain = CLASSE_MAIN.matcher(codigoDoAluno).find();
        boolean temMetodoMain = METODO_MAIN.matcher(codigoDoAluno).find();
        switch (modo) {
            case TRECHO -> {
                if (temClasseMain || temMetodoMain) throw new RequisicaoInvalidaException(
                        "Escreva só o trecho pedido, sem \"public class Main\" e sem o método main: "
                                + "o LogiCup coloca o seu trecho dentro de um programa pronto.");
            }
            case MEMBRO -> {
                if (temClasseMain || temMetodoMain) throw new RequisicaoInvalidaException(
                        "Escreva só o método pedido, sem \"public class Main\" e sem o método main.");
            }
            case CLASSES -> {
                if (temClasseMain) throw new RequisicaoInvalidaException(
                        "Escreva só a(s) classe(s) pedida(s). A classe Main já existe no programa de teste.");
            }
        }
    }
}
