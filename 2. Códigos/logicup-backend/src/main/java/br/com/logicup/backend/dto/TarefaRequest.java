package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Sprint 9: atribuir uma rodada à turma (R11), com prazo opcional (R12). Prazo chega como "2026-10-30". */
public record TarefaRequest(
        @NotNull(message = "Escolha a rodada da tarefa.")
        Integer rodadaId,
        LocalDate prazo
) {
}
