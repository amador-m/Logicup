package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.Avatar;

import java.util.List;

/**
 * Sprint 9: GET /api/avatares (e resposta da compra).
 * gemas = saldo do aluno; null para professor/admin (eles não têm gemas nem compram).
 */
public record AvataresResponse(Integer gemas, Avatar avatarAtual, List<AvatarResponse> avatares) {
}
