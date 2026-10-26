package br.com.logicup.backend.exception;

/** Sprint 9: tentar USAR um avatar que a pessoa não tem (não comprou) -> HTTP 403. */
public class AvatarNaoPossuidoException extends RuntimeException {
    public AvatarNaoPossuidoException() {
        super("Você ainda não tem esse avatar. Compre-o na loja com suas gemas.");
    }
}
