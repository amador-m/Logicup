package br.com.logicup.backend.exception;

/** O recurso pedido (ex.: rodada) não existe -> HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
