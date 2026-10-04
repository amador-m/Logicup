package br.com.logicup.backend.exception;

/** Pedido com dados que não fazem sentido (ex.: exercício de outra rodada) -> HTTP 400. */
public class RequisicaoInvalidaException extends RuntimeException {
    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
