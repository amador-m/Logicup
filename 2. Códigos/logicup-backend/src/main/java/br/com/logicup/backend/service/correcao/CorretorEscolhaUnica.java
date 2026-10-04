package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Arrays;

/** Gabarito {"correta":"a"} | resposta do aluno {"alternativa":"a"} */
@Component
public class CorretorEscolhaUnica extends CorretorBase {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Gabarito(String correta) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resposta(String alternativa) {}

    public CorretorEscolhaUnica(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    public FormatoResposta formato() {
        return FormatoResposta.ESCOLHA_UNICA;
    }

    @Override
    public boolean corrigir(Exercicio exercicio, Object resposta) {
        Gabarito gabarito = lerDoBanco(exercicio.getGabaritoJson(), Gabarito.class, exercicio.getCodigoQuestao());
        Resposta enviada = lerResposta(resposta, Resposta.class);
        exigir(enviada.alternativa() != null, "Escolha uma alternativa.");
        return gabarito.correta().equals(enviada.alternativa());
    }

    @Override
    public Object respostaCorreta(Exercicio exercicio) {
        String correta = lerDoBanco(exercicio.getGabaritoJson(), Gabarito.class, exercicio.getCodigoQuestao()).correta();
        return Arrays.stream(lerDoBanco(exercicio.getAlternativasJson(), Alternativa[].class, exercicio.getCodigoQuestao()))
                .filter(alternativa -> alternativa.id().equals(correta))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Gabarito aponta para alternativa inexistente: " + exercicio.getCodigoQuestao()));
    }
}
