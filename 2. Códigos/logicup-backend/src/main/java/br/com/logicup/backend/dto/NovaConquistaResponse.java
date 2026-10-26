package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.NivelConquista;

import java.time.LocalDateTime;

/**
 * Sprint 9 (M3): uma medalha que o aluno ainda não viu, para a comemoração "Nova conquista!".
 * gemas = quantas gemas ela RENDEU (valor gravado no momento em que foi ganha).
 */
public record NovaConquistaResponse(String codigo, String titulo, NivelConquista nivel, String descricao,
                                    int gemas, LocalDateTime desbloqueadaEm) {
}
