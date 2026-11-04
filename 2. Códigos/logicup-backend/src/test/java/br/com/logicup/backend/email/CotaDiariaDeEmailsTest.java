package br.com.logicup.backend.email;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sprint 10: a cota diária de e-mails (teste de unidade, com números pequenos para ficar legível:
 * alerta 3, reserva 4, limite 5) e o listener respeitando a cota.
 */
class CotaDiariaDeEmailsTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");

    /** Relógio que o teste consegue adiantar (para simular a virada do dia). */
    private static final class RelogioDeTeste extends Clock {
        private Instant agora = Instant.parse("2026-11-04T15:00:00Z"); // 12h em São Paulo
        void adiantar(Duration d) { agora = agora.plus(d); }
        @Override public ZoneId getZone() { return SP; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
    }

    @Test
    void boasVindasParamNaReservaEOsEssenciaisNoLimite() {
        CotaDiariaDeEmails cota = new CotaDiariaDeEmails(new RelogioDeTeste(), 5, 4, 3);
        for (int i = 0; i < 4; i++) cota.registrarEnvio();

        assertFalse(cota.podeEnviar(TipoEmailConta.BOAS_VINDAS), "na reserva, boas-vindas param");
        assertTrue(cota.podeEnviar(TipoEmailConta.CONFIRMAR_EMAIL), "confirmação ainda sai");

        cota.registrarEnvio(); // 5 = limite
        assertFalse(cota.podeEnviar(TipoEmailConta.CONFIRMAR_EMAIL));
        assertFalse(cota.podeEnviar(TipoEmailConta.REDEFINIR_SENHA));
    }

    @Test
    void alertaDisparaUmaVezPorDiaEADiaVirada() {
        RelogioDeTeste relogio = new RelogioDeTeste();
        CotaDiariaDeEmails cota = new CotaDiariaDeEmails(relogio, 5, 4, 3);
        assertFalse(cota.registrarEnvio());
        assertFalse(cota.registrarEnvio());
        assertTrue(cota.registrarEnvio(), "o 3º envio chega ao alerta");
        assertFalse(cota.registrarEnvio(), "não repete o alerta no mesmo dia");

        relogio.adiantar(Duration.ofHours(13)); // 01h do dia seguinte em São Paulo
        assertEquals(0, cota.enviadosHoje(), "à meia-noite de São Paulo a contagem zera");
        assertTrue(cota.podeEnviar(TipoEmailConta.BOAS_VINDAS));
    }

    @Test
    void numerosForaDeOrdemNaoDeixamSubir() {
        assertThrows(IllegalStateException.class,
                () -> new CotaDiariaDeEmails(Clock.system(ZoneOffset.UTC), 300, 310, 240));
    }

    @Test
    void listenerRespeitaACotaEAvisaAEquipeUmaVez() {
        List<MensagemEmail> caixa = new ArrayList<>();
        CotaDiariaDeEmails cota = new CotaDiariaDeEmails(new RelogioDeTeste(), 5, 4, 3);
        EnvioDeEmailListener listener = new EnvioDeEmailListener(caixa::add,
                new ModelosEmail("https://logicup.dev.br"), Runnable::run, cota, "equipe@teste.com");

        for (int i = 0; i < 8; i++) {
            listener.aoSolicitarEmail(new EmailDeContaSolicitado(TipoEmailConta.CONFIRMAR_EMAIL,
                    "aluno" + i + "@teste.com", "Aluno", "COD" + i));
        }

        long avisos = caixa.stream().filter(m -> m.para().equals("equipe@teste.com")).count();
        assertEquals(1, avisos, "um único aviso para a equipe");
        assertEquals(5, caixa.size(), "4 confirmações + 1 aviso = limite de 5; o resto não sai");
    }
}
