package br.com.logicup.backend.exception;

/**
 * Sprint 9 (ADR-001): não foi possível executar o código (executor fora do ar, lento, ocupado ou
 * desligado). Não é culpa do aluno nem do código dele.
 * Se chegar ao Controller, vira HTTP 503 Service Unavailable. Na correção (Enviar), quem chama
 * captura a exceção e usa a reserva por palavras-chave.
 */
public class ExecutorIndisponivelException extends RuntimeException {

    public ExecutorIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public ExecutorIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
