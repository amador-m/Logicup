package br.com.logicup.backend.dto;

import java.util.List;

/** Sprint 9: um item de GET /api/minhas-turmas (Sprint 11: uma por turma) — o aluno vê o nome do professor (não o e-mail) e as tarefas. */
public record MinhaTurmaResponse(Integer id, String nome, String professor, List<TarefaDoAlunoResponse> tarefas) {
}
