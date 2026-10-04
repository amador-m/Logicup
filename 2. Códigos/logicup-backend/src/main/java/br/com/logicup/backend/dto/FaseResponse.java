package br.com.logicup.backend.dto;

import java.util.List;

/** Uma fase (módulo) da trilha com as suas rodadas. */
public record FaseResponse(Integer id, Integer ordem, String titulo, String descricao,
                           List<RodadaResumoResponse> rodadas) {
}
