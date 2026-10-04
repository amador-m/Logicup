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

/** Gabarito {"ordem":["c","b","d","a"]} | resposta do aluno {"ordem":[...]} */
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
        return gabarito.ordem().equals(enviada.ordem()); // mesma sequência, na mesma ordem
    }

    @Override
    public Object respostaCorreta(Exercicio exercicio) {
        Map<String, Alternativa> porId = Arrays.stream(
                        lerDoBanco(exercicio.getAlternativasJson(), Alternativa[].class, exercicio.getCodigoQuestao()))
                .collect(Collectors.toMap(Alternativa::id, Function.identity()));
        return lerDoBanco(exercicio.getGabaritoJson(), Ordem.class, exercicio.getCodigoQuestao()).ordem().stream()
                .map(porId::get)
                .toList(); // itens na ordem certa: [{id, texto}, ...]
    }
}
