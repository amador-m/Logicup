package br.com.logicup.backend.exception;

import java.time.Duration;

/**
 * Sprint 9: passou do limite de tentativas -> HTTP 429 Too Many Requests.
 * Leva o tempo que falta, que vira o cabeçalho Retry-After (em segundos) e o texto da mensagem.
 */
public class MuitasTentativasException extends RuntimeException {

    private final Duration espera;

    public MuitasTentativasException(String mensagem, Duration espera) {
        super(mensagem);
        this.espera = espera;
    }

    /** Segundos até poder tentar de novo, arredondado PARA CIMA (nunca 0: "tente em 0 s" não faz sentido). */
    public long segundosDeEspera() {
        long segundos = espera.toSeconds() + (espera.toNanosPart() > 0 ? 1 : 0);
        return Math.max(1, segundos);
    }
}
