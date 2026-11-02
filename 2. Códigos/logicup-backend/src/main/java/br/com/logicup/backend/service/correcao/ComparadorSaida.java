package br.com.logicup.backend.service.correcao;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sprint 9 (ADR-001): compara a saída do aluno com a esperada, caso a caso.
 * As regras são as MESMAS do gerador gerar_questoes_codigo.py.
 *
 *   EXATA         -> mesmo texto, linha a linha. Ignora espaços no fim das linhas e linhas em branco
 *                    no começo e no fim (diferença invisível não pode reprovar ninguém).
 *   NUMEROS       -> só a sequência de números inteiros ("Missão 2" e "2" valem o mesmo).
 *   ULTIMO_NUMERO -> só o último número exibido ("Total: 3" e "3" valem o mesmo).
 * Cada questão escolhe a regra que faz sentido para o enunciado (ex.: F6.Q9 não diz o formato
 * da mensagem, só pede para "exibir o total").
 */
public final class ComparadorSaida {

    public static final String MARCA_DE_CASO = "<<<LOGICUP-CASO>>>";
    private static final Pattern NUMERO = Pattern.compile("-?\\d+");

    public enum Comparacao { EXATA, NUMEROS, ULTIMO_NUMERO }

    private ComparadorSaida() {}

    public static boolean iguais(Comparacao comparacao, String esperado, String obtido) {
        return switch (comparacao) {
            case EXATA -> normalizar(esperado).equals(normalizar(obtido));
            case NUMEROS -> numeros(esperado).equals(numeros(obtido));
            case ULTIMO_NUMERO -> {
                List<String> e = numeros(esperado);
                List<String> o = numeros(obtido);
                yield !e.isEmpty() && !o.isEmpty() && e.getLast().equals(o.getLast());
            }
        };
    }

    /**
     * Divide a saída do programa de teste nos pedaços de cada caso.
     * O programa imprime a marca ANTES de cada caso; o que vem antes da primeira marca é descartado.
     */
    public static List<String> separarCasos(String saida) {
        String[] partes = (saida == null ? "" : saida).split(Pattern.quote(MARCA_DE_CASO), -1);
        List<String> casos = new ArrayList<>();
        for (int i = 1; i < partes.length; i++) casos.add(partes[i]);
        return casos;
    }

    public static String normalizar(String texto) {
        if (texto == null) return "";
        List<String> linhas = new ArrayList<>();
        for (String linha : texto.replace("\r\n", "\n").split("\n", -1)) linhas.add(linha.stripTrailing());
        while (!linhas.isEmpty() && linhas.getFirst().isEmpty()) linhas.removeFirst();
        while (!linhas.isEmpty() && linhas.getLast().isEmpty()) linhas.removeLast();
        return String.join("\n", linhas);
    }

    private static List<String> numeros(String texto) {
        List<String> encontrados = new ArrayList<>();
        Matcher m = NUMERO.matcher(texto == null ? "" : texto);
        while (m.find()) encontrados.add(m.group());
        return encontrados;
    }
}
