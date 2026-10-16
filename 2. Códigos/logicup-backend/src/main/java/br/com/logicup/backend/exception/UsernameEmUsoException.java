package br.com.logicup.backend.exception;

/** Sprint 7: cadastro (ou edição de perfil) com um username que já pertence a outra conta -> HTTP 409. */
public class UsernameEmUsoException extends RuntimeException {
    public UsernameEmUsoException() {
        super("Este nome de usuário já está em uso. Escolha outro.");
    }
}
