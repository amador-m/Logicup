package br.com.logicup.backend.security;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Tarefa 6: regra de ORIGEM para requisições que alteram dados.
 * É uma classe "pura" (sem Spring, sem HTTP): recebe os valores dos cabeçalhos e devolve uma decisão.
 * Assim a regra inteira é testada em RegrasOrigemTest sem subir a aplicação.
 *
 * REGRA: todo cabeçalho PRESENTE precisa ser compatível; os AUSENTES não aprovam nem reprovam.
 *   Sec-Fetch-Site -> só "same-origin" é compatível (o próprio navegador diz se o pedido veio da nossa origem);
 *   Origin         -> precisa estar EXATAMENTE na lista (esquema, host e porta); "null" nunca é compatível;
 *   Referer        -> a ORIGEM dele (esquema, host e porta) precisa estar na lista.
 *   Um cabeçalho bom NUNCA compensa um ruim (ex.: same-origin + Origin malicioso = bloqueado).
 *   Nenhum dos três presente -> SEM_CABECALHOS: a requisição segue, mas o token CSRF continua obrigatório.
 *
 * Por que a página atacante não consegue burlar: Origin e Sec-Fetch-* são cabeçalhos PROIBIDOS para o
 * JavaScript (só o navegador escreve). Ela consegue esconder o Referer (Referrer-Policy), por isso o
 * Referer é só um sinal complementar.
 */
public final class RegrasOrigem {

    /** Resultado da avaliação. SEM_CABECALHOS não é "aprovado": significa "decida pelo CSRF". */
    public enum Decisao { PERMITIDA, SEM_CABECALHOS, BLOQUEADA }

    private static final String MESMA_ORIGEM = "same-origin";

    private final Set<String> origensConfiaveis;

    /**
     * Valida a lista NA SUBIDA da aplicação: se algo estiver errado, o Spring nem sobe
     * (melhor descobrir agora do que em produção, com a proteção desligada sem ninguém perceber).
     */
    public RegrasOrigem(Collection<String> origens) {
        if (origens == null || origens.isEmpty()) {
            throw new IllegalArgumentException("logicup.seguranca.origens-confiaveis não pode ficar vazia.");
        }
        Set<String> normalizadas = new LinkedHashSet<>();
        for (String origem : origens) {
            normalizadas.add(normalizarConfiavel(origem));
        }
        this.origensConfiaveis = Set.copyOf(normalizadas);
    }

    public Set<String> origensConfiaveis() {
        return origensConfiaveis;
    }

    /** null = cabeçalho ausente. */
    public Decisao avaliar(String secFetchSite, String origin, String referer) {
        if (secFetchSite == null && origin == null && referer == null) {
            return Decisao.SEM_CABECALHOS;
        }
        if (secFetchSite != null && !MESMA_ORIGEM.equals(secFetchSite.trim().toLowerCase(Locale.ROOT))) {
            return Decisao.BLOQUEADA;
        }
        if (origin != null && !confiavel(origemDoCabecalho(origin))) {
            return Decisao.BLOQUEADA;
        }
        if (referer != null && !confiavel(origemDoReferer(referer))) {
            return Decisao.BLOQUEADA;
        }
        return Decisao.PERMITIDA;
    }

    private boolean confiavel(String origemNormalizada) {
        return origemNormalizada != null && origensConfiaveis.contains(origemNormalizada);
    }

    /** Origin vem no formato "esquema://host[:porta]", sem caminho. Qualquer outra coisa (inclusive "null") -> null. */
    private static String origemDoCabecalho(String valor) {
        try {
            return normalizar(valor, false);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Referer é uma URL completa: só interessa a ORIGEM dela (esquema, host, porta). Inválido -> null. */
    private static String origemDoReferer(String valor) {
        try {
            return normalizar(valor, true);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Valida um item da lista de confiáveis, com mensagem clara para quem configura. */
    static String normalizarConfiavel(String origem) {
        if (origem == null || origem.isBlank()) {
            throw new IllegalArgumentException("Origem confiável vazia na lista.");
        }
        String valor = origem.trim();
        if (valor.contains("*") || valor.equalsIgnoreCase("null")) {
            throw new IllegalArgumentException("Origem confiável inválida (curinga ou \"null\" não são permitidos): " + valor);
        }
        return normalizar(valor, false);
    }

    /**
     * "HTTP://LocalHost:5173" -> "http://localhost:5173"; "https://x.com:443" -> "https://x.com".
     * Só http/https, com host, sem usuário/senha, query ou fragmento.
     * Se aceitarCaminho = false (Origin e lista), também não pode ter caminho.
     */
    private static String normalizar(String valor, boolean aceitarCaminho) {
        URI uri;
        try {
            uri = new URI(valor.trim());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Origem com formato inválido: " + valor);
        }
        String esquema = uri.getScheme() == null ? null : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!"http".equals(esquema) && !"https".equals(esquema)) {
            throw new IllegalArgumentException("Origem precisa ser http ou https: " + valor);
        }
        if (uri.getHost() == null || uri.getRawUserInfo() != null) {
            throw new IllegalArgumentException("Origem sem host válido: " + valor);
        }
        boolean temCaminho = uri.getRawPath() != null && !uri.getRawPath().isEmpty();
        if (!aceitarCaminho && (temCaminho || uri.getRawQuery() != null || uri.getRawFragment() != null)) {
            throw new IllegalArgumentException("Origem não pode ter caminho, query ou fragmento: " + valor);
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        int porta = uri.getPort();
        boolean portaPadrao = porta == -1 || ("http".equals(esquema) && porta == 80) || ("https".equals(esquema) && porta == 443);
        return esquema + "://" + host + (portaPadrao ? "" : ":" + porta);
    }
}
