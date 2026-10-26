package br.com.logicup.backend.dto;

import java.util.List;

/** Sprint 9: GET /api/minha-turma — o aluno vê o nome do professor (não o e-mail) e as tarefas. */
public record MinhaTurmaResponse(Integer id, String nome, String professor, List<TarefaDoAlunoResponse> tarefas) {
}
