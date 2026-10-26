package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.Avatar;

/** Sprint 9: um avatar do catálogo, do ponto de vista de quem está logado. */
public record AvatarResponse(Avatar codigo, String nome, int preco, boolean gratuito, boolean possui, boolean emUso) {
}
