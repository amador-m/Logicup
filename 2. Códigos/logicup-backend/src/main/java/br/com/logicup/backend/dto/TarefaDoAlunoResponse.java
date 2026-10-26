package br.com.logicup.backend.dto;

import java.time.LocalDate;

/** Sprint 9: uma tarefa na visão do ALUNO. "atrasada" = prazo vencido e ainda não concluída (R12). */
public record TarefaDoAlunoResponse(Integer id, Integer rodadaId, String rodada, String fase,
                                    LocalDate prazo, StatusTarefa status, boolean atrasada) {
}
