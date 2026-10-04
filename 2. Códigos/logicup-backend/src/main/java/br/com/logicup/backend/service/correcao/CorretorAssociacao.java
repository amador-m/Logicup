package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Alternativas {"itens":[{id,texto}], "opcoes":[{id,texto}]}
 * Gabarito {"pares":{"1":"a","2":"b"}} | resposta do aluno {"pares":{...}}
 */
@Component
public class CorretorAssociacao extends CorretorBase {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pares(Map<String, String> pares) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Alternativas(List<Alternativa> itens, List<Alternativa> opcoes) {}

    /** Um par certo, já com os TEXTOS, para exibir ao aluno. */
    public record ParCorreto(String item, String opcao) {}

    public CorretorAssociacao(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    public FormatoResposta formato() {
        return FormatoResposta.ASSOCIACAO;
    }

    @Override
    public boolean corrigir(Exercicio exercicio, Object resposta) {
        Pares gabarito = lerDoBanco(exercicio.getGabaritoJson(), Pares.class, exercicio.getCodigoQuestao());
        Pares enviada = lerResposta(resposta, Pares.class);
        exigir(enviada.pares() != null, "Associe os itens.");
        return gabarito.pares().equals(enviada.pares()); // todos os pares iguais (a ordem das chaves não importa)
    }

    @Override
    public Object respostaCorreta(Exercicio exercicio) {
        Alternativas alternativas = lerDoBanco(exercicio.getAlternativasJson(), Alternativas.class, exercicio.getCodigoQuestao());
        Map<String, String> textoItem = alternativas.itens().stream()
                .collect(Collectors.toMap(Alternativa::id, Alternativa::texto));
        Map<String, Alternativa> opcaoPorId = alternativas.opcoes().stream()
                .collect(Collectors.toMap(Alternativa::id, Function.identity()));

        return lerDoBanco(exercicio.getGabaritoJson(), Pares.class, exercicio.getCodigoQuestao()).pares().entrySet().stream()
                .map(par -> new ParCorreto(textoItem.get(par.getKey()), opcaoPorId.get(par.getValue()).texto()))
                .toList();
    }
}
