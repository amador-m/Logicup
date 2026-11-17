package br.com.logicup.backend.exception;

/** Sprint 11: o nivelamento é oferecido uma vez só (no primeiro acesso) -> HTTP 409. */
public class NivelamentoJaRealizadoException extends RuntimeException {
    public NivelamentoJaRealizadoException() {
        super("O nivelamento só pode ser feito no primeiro acesso, antes de começar a trilha.");
    }
}
