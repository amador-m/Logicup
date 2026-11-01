package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gabarito {"ordem":["c","b","d","a"]} | resposta do aluno {"ordem":[...]}
 *
 * Sprint 9: a comparação é feita pelo TEXTO dos itens, não pelo id.
 * Motivo: alguns exercícios têm itens com o mesmo texto (as duas linhas "}" da F2.Q4).
 * Para o aluno elas são idênticas; trocar uma pela outra não pode contar como erro.
 */
@Component
public class CorretorOrdenacao extends CorretorBase {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ordem(List<String> ordem) {}

    public CorretorOrdenacao(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    public FormatoResposta formato() {
        return FormatoResposta.ORDENACAO;
    }

    @Override
    public boolean corrigir(Exercicio exercicio, Object resposta) {
        Ordem gabarito = lerDoBanco(exercicio.getGabaritoJson(), Ordem.class, exercicio.getCodigoQuestao());
        Ordem enviada = lerResposta(resposta, Ordem.class);
        exigir(enviada.ordem() != null, "Informe a ordem dos itens.");

        // 1) A resposta precisa usar CADA item exatamente uma vez (ser uma permutação dos ids).
        //    Sem isto, comparar só os textos deixaria passar ["a","a",...] repetindo uma "}".
        if (!ordenados(enviada.ordem()).equals(ordenados(gabarito.ordem()))) {
            return false;
        }

        // 2) Compara a sequência de TEXTOS: itens de texto igual ficam intercambiáveis.
        Map<String, String> textoPorId = textosPorId(exercicio);
        return enviada.ordem().stream().map(textoPorId::get).toList()
                .equals(gabarito.ordem().stream().map(textoPorId::get).toList());
    }

    @Override
    public Object respostaCorreta(Exercicio exercicio) {
        Map<String, Alternativa> porId = alternativasPorId(exercicio);
        return lerDoBanco(exercicio.getGabaritoJson(), Ordem.class, exercicio.getCodigoQuestao()).ordem().stream()
                .map(porId::get)
                .toList(); // itens na ordem certa: [{id, texto}, ...]
    }

    private static List<String> ordenados(List<String> ids) {
        return ids.stream().sorted().toList();
    }

    private Map<String, Alternativa> alternativasPorId(Exercicio exercicio) {
        return Arrays.stream(lerDoBanco(exercicio.getAlternativasJson(), Alternativa[].class, exercicio.getCodigoQuestao()))
                .collect(Collectors.toMap(Alternativa::id, Function.identity()));
    }

    /** id -> texto, sem espaços nas pontas ("}" e "} " contam como o mesmo item). */
    private Map<String, String> textosPorId(Exercicio exercicio) {
        return alternativasPorId(exercicio).values().stream()
                .collect(Collectors.toMap(Alternativa::id, alternativa -> alternativa.texto().strip()));
    }
}
