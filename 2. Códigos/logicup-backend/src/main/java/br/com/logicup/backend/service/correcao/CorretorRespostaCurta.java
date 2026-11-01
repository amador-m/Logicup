package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

/**
 * RESPOSTA_CURTA tem dois formatos de gabarito:
 *
 * 1) UMA lacuna (formato original, Sprint 6):
 *    gabarito {"aceitas":["27","vinte e sete"], "diferenciarMaiusculas":false}
 *    resposta {"texto":"27"}
 *
 * 2) VÁRIAS lacunas (Sprint 9, D3 — ex.: F3.Q3 "___ idade = ___;"):
 *    gabarito {"lacunas":[{"aceitas":["int"]},{"aceitas":["18"]}], "diferenciarMaiusculas":true}
 *    resposta {"lacunas":["int","18"]}        (uma posição por lacuna, na ordem do código)
 *    O frontend sabe QUANTAS lacunas existem por alternativas_json = {"lacunas": 2} (público);
 *    as respostas aceitas ficam só no gabarito (secreto).
 *
 * O formato é escolhido pelo GABARITO: se ele tem "lacunas", é o formato 2. Assim todas as questões
 * antigas continuam funcionando sem nenhuma alteração.
 */
@Component
public class CorretorRespostaCurta extends CorretorBase {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Lacuna(List<String> aceitas) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Gabarito(List<String> aceitas, List<Lacuna> lacunas, Boolean diferenciarMaiusculas) {

        boolean temVariasLacunas() {
            return lacunas != null && !lacunas.isEmpty();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resposta(String texto, List<String> lacunas) {}

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
        boolean diferenciar = Boolean.TRUE.equals(gabarito.diferenciarMaiusculas()); // X2 (padrão: não diferencia)

        if (gabarito.temVariasLacunas()) {
            // Quantidade errada de lacunas = requisição malformada (o frontend sempre manda todas) -> 400
            exigir(enviada.lacunas() != null && enviada.lacunas().size() == gabarito.lacunas().size(),
                    "Preencha todas as lacunas.");
            // Correta só se TODAS as lacunas estiverem certas (não há acerto parcial: XP é por questão)
            for (int i = 0; i < gabarito.lacunas().size(); i++) {
                if (!aceita(enviada.lacunas().get(i), gabarito.lacunas().get(i).aceitas(), diferenciar)) {
                    return false;
                }
            }
            return true;
        }

        exigir(enviada.texto() != null, "Digite a sua resposta.");
        return aceita(enviada.texto(), gabarito.aceitas(), diferenciar);
    }

    /** A resposta digitada (já normalizada) é igual a alguma das aceitas (normalizadas do mesmo jeito)? */
    private static boolean aceita(String digitada, List<String> aceitas, boolean diferenciar) {
        String limpa = NormalizadorTexto.paraRespostaCurta(digitada, diferenciar);
        return !limpa.isEmpty() && aceitas.stream()
                .anyMatch(aceitaUma -> NormalizadorTexto.paraRespostaCurta(aceitaUma, diferenciar).equals(limpa));
    }

    /**
     * Para EXIBIR depois da correção (T1):
     *   uma lacuna    -> "27"                (a 1ª resposta aceita)
     *   várias lacunas -> ["int", "18"]      (a 1ª aceita de cada lacuna, na ordem)
     */
    @Override
    public Object respostaCorreta(Exercicio exercicio) {
        Gabarito gabarito = lerDoBanco(exercicio.getGabaritoJson(), Gabarito.class, exercicio.getCodigoQuestao());
        if (gabarito.temVariasLacunas()) {
            return gabarito.lacunas().stream().map(lacuna -> lacuna.aceitas().get(0)).toList();
        }
        return gabarito.aceitas().get(0);
    }
}
