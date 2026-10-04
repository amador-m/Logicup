package br.com.logicup.backend.exception;

/** Aluno tentou abrir/responder uma rodada que ainda não liberou -> HTTP 403. */
public class RodadaBloqueadaException extends RuntimeException {
    public RodadaBloqueadaException() {
        super("Esta rodada ainda está bloqueada. Conclua a rodada anterior para liberá-la.");
    }
}
