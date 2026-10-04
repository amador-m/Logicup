package br.com.logicup.backend.exception;

/** Token válido, mas o usuário dele não existe mais (ex.: conta apagada) -> HTTP 401. */
public class SessaoInvalidaException extends RuntimeException {
    public SessaoInvalidaException() {
        super("Sessão inválida. Faça login novamente.");
    }
}
