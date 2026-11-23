package br.com.logicup.backend.exception;

/**
 * Sprint 11: o aluno usou o código de uma turma em que JÁ está (matrícula ativa) -> HTTP 409.
 * Substitui a AlunoJaTemTurmaException (Sprint 9, R5), que barrava entrar em uma SEGUNDA turma:
 * agora o aluno pode estar em várias turmas, mas não duas vezes na mesma.
 */
public class AlunoJaEstaNaTurmaException extends RuntimeException {
    public AlunoJaEstaNaTurmaException() {
        super("Você já está nesta turma.");
    }
}
