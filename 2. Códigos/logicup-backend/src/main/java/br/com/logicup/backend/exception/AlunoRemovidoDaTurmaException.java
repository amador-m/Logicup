package br.com.logicup.backend.exception;

/** Sprint 9 (R7): aluno removido pelo professor tentando voltar pelo código -> HTTP 403. */
public class AlunoRemovidoDaTurmaException extends RuntimeException {
    public AlunoRemovidoDaTurmaException() {
        super("Você foi removido desta turma pelo professor e não pode entrar de novo pelo código.");
    }
}
