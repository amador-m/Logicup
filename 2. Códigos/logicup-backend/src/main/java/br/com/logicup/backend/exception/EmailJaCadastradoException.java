package br.com.logicup.backend.exception;

/** Cadastro com e-mail que já pertence a outra conta -> HTTP 409. */
public class EmailJaCadastradoException extends RuntimeException {
    public EmailJaCadastradoException() {
        super("Este e-mail já está cadastrado.");
    }
}
