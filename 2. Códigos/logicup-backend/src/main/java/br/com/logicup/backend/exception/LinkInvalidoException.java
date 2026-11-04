package br.com.logicup.backend.exception;

/**
 * Sprint 10: link de confirmação/troca de senha que não existe, já foi usado ou venceu -> HTTP 400.
 * A MESMA mensagem para todos os casos: quem tenta adivinhar códigos não descobre nada.
 */
public class LinkInvalidoException extends RuntimeException {

    public static final String CODIGO = "LINK_INVALIDO";

    public LinkInvalidoException() {
        super("Este link é inválido ou já expirou. Peça um novo.");
    }
}
