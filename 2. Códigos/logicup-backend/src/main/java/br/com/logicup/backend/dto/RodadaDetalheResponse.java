package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.StatusRodada;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.List;

/** GET /api/rodadas/{id}: objetivos, teoria, exemplos (conteudo) e exercícios, sem gabarito. */
public record RodadaDetalheResponse(Integer id, Integer ordem, String titulo,
                                    Integer faseOrdem, String faseTitulo, StatusRodada status,
                                    @JsonRawValue String conteudo,
                                    List<ExercicioResponse> exercicios) {
}
