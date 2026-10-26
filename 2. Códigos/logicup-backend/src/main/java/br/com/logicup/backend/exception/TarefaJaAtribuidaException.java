package br.com.logicup.backend.exception;

/** Sprint 9 (R11): a rodada já é uma tarefa desta turma -> HTTP 409. */
public class TarefaJaAtribuidaException extends RuntimeException {
    public TarefaJaAtribuidaException() {
        super("Esta rodada já foi atribuída a esta turma.");
    }
}
