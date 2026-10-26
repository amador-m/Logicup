package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.NivelConquista;

import java.time.LocalDateTime;

/** Sprint 9: um nível de uma conquista. desbloqueadaEm = null enquanto o aluno não ganhou esse nível. */
public record NivelConquistaResponse(NivelConquista nivel, int meta, String descricao, int gemas,
                                     LocalDateTime desbloqueadaEm) {
}
