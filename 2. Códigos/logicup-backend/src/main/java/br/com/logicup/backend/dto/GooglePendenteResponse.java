package br.com.logicup.backend.dto;

/** Sprint 10: para a tela "Complete seu cadastro" mostrar o e-mail (fixo) e sugerir o nome que veio do Google. */
public record GooglePendenteResponse(String nome, String email) {
}
