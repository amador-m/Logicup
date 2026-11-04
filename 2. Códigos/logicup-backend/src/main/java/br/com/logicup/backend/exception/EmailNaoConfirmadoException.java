package br.com.logicup.backend.exception;

/**
 * Sprint 10 (decisão C): a senha está CERTA, mas o e-mail ainda não foi confirmado -> HTTP 403.
 * Só é lançada depois de conferir a senha: assim ela não revela, para quem não sabe a senha,
 * se um e-mail tem conta ou não. O "codigo" deixa o frontend mostrar o botão "Reenviar e-mail".
 */
public class EmailNaoConfirmadoException extends RuntimeException {

    public static final String CODIGO = "EMAIL_NAO_CONFIRMADO";

    private final String email;

    public EmailNaoConfirmadoException(String email) {
        super("Falta confirmar seu e-mail. Abra o link que enviamos no cadastro ou peça um novo.");
        this.email = email;
    }

    public String getEmail() { return email; }
}
