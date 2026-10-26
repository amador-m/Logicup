package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.Avatar;

import java.time.LocalDate;

/**
 * Sprint 9 (R9): o que o professor vê de cada aluno ATIVO da turma.
 * Decisão A3: inclui o e-mail. Nunca inclui senha, hash ou dados de outras turmas.
 */
public record AlunoDaTurmaResponse(Integer id, String nome, String username, String email,
                                   int xp, int nivel, int rodadasAprovadas, LocalDate ultimaAtividade,
                                   Avatar avatar) { // Sprint 9
}
