package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/** Gabarito {"aceitas":["27","vinte e sete"], "diferenciarMaiusculas":false} | resposta {"texto":"27"} */
@Component
public class CorretorRespostaCurta extends CorretorBase {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Gabarito(List<String> aceitas, Boolean diferenciarMaiusculas) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resposta(String texto) {}

    public CorretorRespostaCurta(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    public FormatoResposta formato() {
        return FormatoResposta.RESPOSTA_CURTA;
    }

    @Override
    public boolean corrigir(Exercicio exercicio, Object resposta) {
        Gabarito gabarito = lerDoBanco(exercicio.getGabaritoJson(), Gabarito.class, exercicio.getCodigoQuestao());
        Resposta enviada = lerResposta(resposta, Resposta.class);
        exigir(enviada.texto() != null, "Digite a sua resposta.");

        boolean diferenciar = Boolean.TRUE.equals(gabarito.diferenciarMaiusculas()); // X2 (padrão: não diferencia)
        String digitada = NormalizadorTexto.paraRespostaCurta(enviada.texto(), diferenciar);
        return !digitada.isEmpty() && gabarito.aceitas().stream()
                .anyMatch(aceita -> NormalizadorTexto.paraRespostaCurta(aceita, diferenciar).equals(digitada));
    }

    @Override
    public Object respostaCorreta(Exercicio exercicio) {
        return lerDoBanco(exercicio.getGabaritoJson(), Gabarito.class, exercicio.getCodigoQuestao()).aceitas().get(0);
    }
}
