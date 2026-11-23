package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotNull;

/** Sprint 11: PUT /api/admin/usuarios/{id}/bloqueio -> { "bloqueado": true } bloqueia; false desbloqueia. */
public record BloqueioRequest(@NotNull(message = "Informe se a conta deve ficar bloqueada.") Boolean bloqueado) {
}
