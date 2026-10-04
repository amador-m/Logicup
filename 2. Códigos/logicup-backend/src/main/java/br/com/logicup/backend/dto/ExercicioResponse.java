package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.TipoExercicio;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.List;

/**
 * Um exercício como o ALUNO o vê. NÃO existe campo de gabarito aqui, de propósito:
 * se não está no DTO, não tem como vazar na API.
 *
 * @JsonRawValue: "alternativas" já é JSON no banco; é enviado como está (objeto/lista), não como texto.
 */
public record ExercicioResponse(Integer id, String codigo, String titulo, Integer ordem,
                                TipoExercicio tipo, FormatoResposta formato, Integer pontos,
                                String enunciado, String codigoApoio,
                                @JsonRawValue String alternativas,
                                List<String> dicas,
                                boolean concluido, int tentativas) {
}
