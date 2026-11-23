package br.com.logicup.backend.admin;

import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Sprint 11: regras PURAS da edição de conteúdo pelo admin (sem banco, sem Spring -> teste de unidade simples).
 *
 * A teoria de cada rodada é um JSON: {"objetivos": [...], "teoria": [{"titulo", "texto", "blocos": [...]}],
 * "exemplos": []}. Cada bloco é um parágrafo, uma lista, um código ou uma tabela.
 *
 * Decisão da equipe: o admin edita os TEXTOS, não a ESTRUTURA. Por isso o JSON novo precisa ter exatamente
 * a mesma "forma" do atual: os mesmos campos, as listas com o mesmo tamanho e os blocos com o mesmo tipo.
 * Só o conteúdo dos textos pode mudar. Assim a tela da missão (que desenha cada tipo de bloco) nunca recebe
 * algo que não sabe mostrar, e um erro na edição não "quebra" a rodada.
 */
public final class RegrasEdicaoConteudo {

    static final int TAMANHO_MAXIMO_TEXTO = 20_000;  // um parágrafo/código gigante é quase certamente engano

    private RegrasEdicaoConteudo() {}

    /**
     * Confere se "novo" tem a mesma forma de "atual" (recursivamente). Textos podem mudar; todo o resto não.
     * O campo "tipo" dos blocos e o campo "texto" das seções (recalculado depois) são tratados à parte.
     *
     * @param caminho onde estamos no JSON (para a mensagem de erro dizer exatamente o que está errado)
     */
    public static void exigirMesmaForma(JsonNode atual, JsonNode novo, String caminho) {
        if (novo == null || novo.isNull()) {
            throw invalido(caminho, "está faltando");
        }
        if (atual.isString()) {
            if (!novo.isString()) {
                throw invalido(caminho, "deveria ser um texto");
            }
            if (novo.stringValue().length() > TAMANHO_MAXIMO_TEXTO) {
                throw invalido(caminho, "passou de " + TAMANHO_MAXIMO_TEXTO + " caracteres");
            }
            return;
        }
        if (atual.isArray()) {
            if (!novo.isArray() || novo.size() != atual.size()) {
                throw invalido(caminho, "precisa ter " + atual.size() + " itens (a edição não adiciona nem remove itens)");
            }
            for (int i = 0; i < atual.size(); i++) {
                exigirMesmaForma(atual.get(i), novo.get(i), caminho + "[" + i + "]");
            }
            return;
        }
        if (atual.isObject()) {
            if (!novo.isObject() || !new HashSet<>(atual.propertyNames()).equals(new HashSet<>(novo.propertyNames()))) {
                throw invalido(caminho, "tem campos diferentes dos esperados");
            }
            for (String campo : atual.propertyNames()) {
                if ("tipo".equals(campo) && !atual.get(campo).equals(novo.get(campo))) {
                    throw invalido(caminho + ".tipo", "não pode mudar");
                }
                exigirMesmaForma(atual.get(campo), novo.get(campo), caminho + "." + campo);
            }
            return;
        }
        // números, true/false: não são editáveis
        if (!atual.equals(novo)) {
            throw invalido(caminho, "não pode mudar");
        }
    }

    /** Títulos e objetivos não podem ficar vazios (um parágrafo vazio também não faz sentido). */
    public static void exigirTextosPreenchidos(JsonNode no, String caminho) {
        if (no.isString()) {
            if (no.stringValue().isBlank()) {
                throw invalido(caminho, "não pode ficar vazio");
            }
        } else if (no.isArray()) {
            for (int i = 0; i < no.size(); i++) {
                exigirTextosPreenchidos(no.get(i), caminho + "[" + i + "]");
            }
        } else if (no.isObject()) {
            if ("tabela".equals(no.path("tipo").asString(""))) {
                return; // células de tabela podem ser vazias de propósito (ex.: o canto do cabeçalho)
            }
            for (String campo : no.propertyNames()) {
                if (!"texto".equals(campo) || !no.has("blocos")) { // o "texto" da seção é recalculado
                    exigirTextosPreenchidos(no.get(campo), caminho + "." + campo);
                }
            }
        }
    }

    /**
     * Recalcula o "texto" de cada seção a partir dos blocos, com a MESMA regra do gerador do seed
     * (gerar_seed.py): parágrafo e código como estão, item de lista com "- ", linha de tabela com " | ".
     * Esse texto é a versão "simples" da seção (usado, por exemplo, para estimar o tempo de leitura).
     */
    public static void recalcularTextoDasSecoes(JsonNode conteudo) {
        JsonNode teoria = conteudo.get("teoria");
        if (teoria == null || !teoria.isArray()) {
            return;
        }
        for (JsonNode secao : teoria) {
            if (!(secao instanceof ObjectNode objeto) || !secao.has("blocos")) {
                continue;
            }
            List<String> linhas = new ArrayList<>();
            for (JsonNode bloco : secao.get("blocos")) {
                switch (bloco.path("tipo").asString("")) {
                    case "lista" -> bloco.path("itens").forEach(item -> linhas.add("- " + item.asString()));
                    case "codigo" -> linhas.add(bloco.path("codigo").asString());
                    case "tabela" -> bloco.path("linhas").forEach(linha -> {
                        List<String> celulas = new ArrayList<>();
                        ((ArrayNode) linha).forEach(celula -> celulas.add(celula.asString()));
                        linhas.add(String.join(" | ", celulas));
                    });
                    default -> linhas.add(bloco.path("texto").asString());
                }
            }
            objeto.put("texto", String.join("\n", linhas));
        }
    }

    private static RequisicaoInvalidaException invalido(String caminho, String problema) {
        return new RequisicaoInvalidaException("Conteúdo inválido: " + caminho + " " + problema + ".");
    }
}
