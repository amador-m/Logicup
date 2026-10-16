package br.com.logicup.backend.exception;

/** Sprint 7: a "nova" senha é igual à atual -> HTTP 400 no campo novaSenha. */
public class NovaSenhaIgualException extends RuntimeException {
    public NovaSenhaIgualException() {
        super("A nova senha deve ser diferente da atual.");
    }
}
