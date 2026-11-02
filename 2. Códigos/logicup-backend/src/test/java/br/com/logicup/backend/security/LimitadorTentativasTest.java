package br.com.logicup.backend.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 9: testes de UNIDADE do limitador (sem Spring, sem banco).
 * O RelogioDeTeste deixa o teste "avançar o tempo" na hora que quiser, sem esperar de verdade.
 */
class LimitadorTentativasTest {

    /** Relógio que só anda quando o teste manda. */
    static class RelogioDeTeste extends Clock {
        private Instant agora = Instant.parse("2026-11-01T12:00:00Z");

        void avancar(Duration tempo) { agora = agora.plus(tempo); }

        @Override public Instant instant() { return agora; }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
    }

    private final RelogioDeTeste relogio = new RelogioDeTeste();
    private final LimitadorTentativas limitador = new LimitadorTentativas(3, Duration.ofMinutes(15), relogio);

    @Test
    void liberaAteOMaximoEBloqueiaAoAtingirOMaximo() {
        limitador.registrarErro("ana");
        limitador.registrarErro("ana");
        assertTrue(limitador.bloqueioRestante("ana").isEmpty(), "2 erros de 3: ainda pode tentar");

        limitador.registrarErro("ana");
        assertTrue(limitador.bloqueioRestante("ana").isPresent(), "3 erros de 3: bloqueado");
    }

    @Test
    void informaQuantoTempoFaltaContandoDoPrimeiroErro() {
        limitador.registrarErro("ana");
        relogio.avancar(Duration.ofMinutes(5));
        limitador.registrarErro("ana");
        limitador.registrarErro("ana");

        // a janela começou no 1º erro: 15 min - 5 min = faltam 10 min
        assertEquals(Optional.of(Duration.ofMinutes(10)), limitador.bloqueioRestante("ana"));
    }

    @Test
    void desbloqueiaQuandoAJanelaAcabaERecomecaDoZero() {
        for (int i = 0; i < 3; i++) limitador.registrarErro("ana");
        relogio.avancar(Duration.ofMinutes(15));
        assertTrue(limitador.bloqueioRestante("ana").isEmpty(), "a janela acabou: liberado");

        limitador.registrarErro("ana"); // abre uma janela NOVA, com 1 erro
        assertTrue(limitador.bloqueioRestante("ana").isEmpty());
    }

    @Test
    void errosAntigosForaDaJanelaNaoSomam() {
        limitador.registrarErro("ana");
        limitador.registrarErro("ana");
        relogio.avancar(Duration.ofMinutes(16)); // os 2 erros "venceram"
        limitador.registrarErro("ana");
        limitador.registrarErro("ana");
        assertTrue(limitador.bloqueioRestante("ana").isEmpty(), "só 2 erros na janela atual");
    }

    @Test
    void cadaChaveTemSeuProprioContador() {
        for (int i = 0; i < 3; i++) limitador.registrarErro("ana");
        assertTrue(limitador.bloqueioRestante("ana").isPresent());
        assertTrue(limitador.bloqueioRestante("bruno").isEmpty(), "o bloqueio da Ana não atinge o Bruno");
    }

    @Test
    void limparZeraOContador() {
        for (int i = 0; i < 3; i++) limitador.registrarErro("ana");
        limitador.limpar("ana");
        assertTrue(limitador.bloqueioRestante("ana").isEmpty());
    }

    @Test
    void removerVencidasApagaSoAsJanelasQueAcabaram() {
        limitador.registrarErro("antiga");
        relogio.avancar(Duration.ofMinutes(20));
        limitador.registrarErro("recente");

        limitador.removerVencidas();

        assertEquals(1, limitador.chavesAcompanhadas(), "só a janela recente continua na memória");
    }

    @Test
    void recusaConfiguracaoInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new LimitadorTentativas(0, Duration.ofMinutes(1), relogio));
        assertThrows(IllegalArgumentException.class, () -> new LimitadorTentativas(3, Duration.ZERO, relogio));
    }

    @Test
    void mensagemDeMinutosArredondaParaCima() {
        assertEquals("1 minuto", ProtecaoForcaBruta.minutos(Duration.ofSeconds(30)));
        assertEquals("2 minutos", ProtecaoForcaBruta.minutos(Duration.ofSeconds(61)));
        assertEquals("15 minutos", ProtecaoForcaBruta.minutos(Duration.ofMinutes(15)));
    }
}
