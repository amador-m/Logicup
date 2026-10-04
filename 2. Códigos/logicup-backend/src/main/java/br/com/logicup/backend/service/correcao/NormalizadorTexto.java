package br.com.logicup.backend.service.correcao;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Regras de normalização de texto usadas na correção (X2 e X3). */
public final class NormalizadorTexto {

    private static final Pattern ACENTOS = Pattern.compile("\\p{M}+");     // marcas de acento após a decomposição
    private static final Pattern ESPACOS = Pattern.compile("\\s+");
    private static final Pattern PONTOS_FINAIS = Pattern.compile("\\.+$");

    private NormalizadorTexto() {}

    /**
     * TEXTO_LIVRE (X3): minúsculas, sem acentos e com espaços simples.
     * "Veículo  PASSANDO" -> "veiculo passando".
     * NFD separa a letra do acento ("í" vira "i" + "´"), e aí removemos o acento.
     */
    public static String paraPalavrasChave(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcentos = ACENTOS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        return juntarEspacos(semAcentos.toLowerCase(Locale.ROOT));
    }

    /**
     * RESPOSTA_CURTA (X2): tira espaços extras e ponto(s) final(is); maiúsculas conforme a flag da questão.
     * Acentos são mantidos (em código, um acento pode fazer diferença).
     */
    public static String paraRespostaCurta(String texto, boolean diferenciarMaiusculas) {
        if (texto == null) {
            return "";
        }
        String limpo = PONTOS_FINAIS.matcher(juntarEspacos(texto)).replaceAll("").trim();
        return diferenciarMaiusculas ? limpo : limpo.toLowerCase(Locale.ROOT);
    }

    private static String juntarEspacos(String texto) {
        return ESPACOS.matcher(texto).replaceAll(" ").trim();
    }
}
