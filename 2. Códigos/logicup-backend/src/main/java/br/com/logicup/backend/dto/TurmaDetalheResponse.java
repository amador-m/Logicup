package br.com.logicup.backend.dto;

import java.util.List;

/** Sprint 9: GET /api/turmas/{id} — a turma com os alunos ativos e as tarefas. */
public record TurmaDetalheResponse(Integer id, String nome, String codigo, int alunosAtivos,
                                   List<AlunoDaTurmaResponse> alunos, List<TarefaDaTurmaResponse> tarefas) {
}
