package br.com.logicup.backend.exception;

/**
 * Login falhou -> HTTP 401. A MESMA mensagem para e-mail/username inexistente e senha errada (AU7),
 * para ninguém descobrir quais e-mails têm conta (enumeração de usuários).
 */
public class CredenciaisInvalidasException extends RuntimeException {
    public CredenciaisInvalidasException() {
        super("Login ou senha inválidos.");
    }
}
