package br.com.logicup.backend.dto;

import java.util.List;

/** Sprint 11: a árvore do conteúdo para o admin escolher o que editar (fase -> rodada -> exercício). */
public record ConteudoAdminResponse(List<Fase> fases) {

    public record Fase(Integer id, Integer ordem, String titulo, List<Rodada> rodadas) {
    }

    public record Rodada(Integer id, Integer ordem, String titulo, List<Exercicio> exercicios) {
    }

    public record Exercicio(Integer id, String codigo, String titulo) {
    }
}
