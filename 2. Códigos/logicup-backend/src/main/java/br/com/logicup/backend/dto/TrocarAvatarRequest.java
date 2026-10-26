package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Sprint 9: PUT /api/perfil/avatar. O código chega como TEXTO (e não como enum) de propósito:
 * assim um código inexistente vira uma mensagem clara (400), e não um erro genérico de JSON.
 */
public record TrocarAvatarRequest(@NotBlank(message = "Escolha um avatar.") String codigo) {
}
