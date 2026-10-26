package br.com.logicup.backend.exception;

/** Sprint 9 (loja): o avatar é gratuito ou já foi comprado -> HTTP 409 (não há nada a comprar). */
public class AvatarJaPossuidoException extends RuntimeException {
    public AvatarJaPossuidoException() {
        super("Esse avatar já é seu.");
    }
}
