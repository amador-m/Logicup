package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * L3: palavras-chave em GRUPOS de sinônimos.
 * Gabarito {"palavrasChave":[["faixa","pedestre"],["olh","observ"]], "minimo":3, "respostaModelo":"..."}
 * Resposta do aluno {"texto":"..."}
 * Cada grupo conta no máximo 1 vez; é correta quando grupos encontrados >= minimo.
 * Sinônimo novo = só editar o JSON; esta classe não muda.
 */
@Component
public class CorretorTextoLivre extends CorretorBase {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Gabarito(List<List<String>> palavrasChave, Integer minimo, String respostaModelo) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resposta(String texto) {}

    public CorretorTextoLivre(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    public FormatoResposta formato() {
        return FormatoResposta.TEXTO_LIVRE;
    }

    @Override
    public boolean corrigir(Exercicio exercicio, Object resposta) {
        Gabarito gabarito = lerDoBanco(exercicio.getGabaritoJson(), Gabarito.class, exercicio.getCodigoQuestao());
        Resposta enviada = lerResposta(resposta, Resposta.class);
        exigir(enviada.texto() != null, "Digite a sua resposta.");

        String texto = NormalizadorTexto.paraPalavrasChave(enviada.texto());
        long gruposEncontrados = gabarito.palavrasChave().stream()
                .filter(grupo -> grupo.stream()
                        .map(NormalizadorTexto::paraPalavrasChave) // mesma normalização dos dois lados
                        .anyMatch(texto::contains))
                .count();
        return gruposEncontrados >= gabarito.minimo();
    }

    @Override
    public Object respostaCorreta(Exercicio exercicio) {
        // SÓ a resposta modelo: mostrar as palavras-chave permitiria "acertar" digitando a lista
        return lerDoBanco(exercicio.getGabaritoJson(), Gabarito.class, exercicio.getCodigoQuestao()).respostaModelo();
    }
}
