package br.com.logicup.backend.exception;

/** Sprint 9 (R5): aluno com matrícula ativa tentando entrar em (outra) turma -> HTTP 409. */
public class AlunoJaTemTurmaException extends RuntimeException {
    public AlunoJaTemTurmaException() {
        super("Você já está em uma turma. Saia dela antes de entrar em outra.");
    }
}
