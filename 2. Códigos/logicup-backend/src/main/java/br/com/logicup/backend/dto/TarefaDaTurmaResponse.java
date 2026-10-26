package br.com.logicup.backend.dto;

import java.time.LocalDate;

/** Sprint 9: uma tarefa na visão do PROFESSOR — quantos alunos ativos já aprovaram a rodada. */
public record TarefaDaTurmaResponse(Integer id, Integer rodadaId, String rodada, String fase,
                                    LocalDate prazo, int concluiram) {
}
