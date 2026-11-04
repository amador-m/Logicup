package br.com.logicup.backend.email;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Sprint 10: o conteúdo dos e-mails (teste de unidade: sem Spring, sem banco). */
class ModelosEmailTest {

    private final ModelosEmail modelos = new ModelosEmail("https://logicup.dev.br/");

    @Test
    void confirmacaoLevaOCodigoNoFragmentoDoLink() {
        MensagemEmail m = modelos.montar(new EmailDeContaSolicitado(TipoEmailConta.CONFIRMAR_EMAIL,
                "ana@teste.com", "Ana Paula", "ABC_123-xyz"));
        assertEquals("ana@teste.com", m.para());
        String link = "https://logicup.dev.br/confirmar-email#ABC_123-xyz"; // sem "//" mesmo com "/" no final da URL
        assertTrue(m.texto().contains(link));
        assertTrue(m.html().contains(link));
        assertTrue(m.texto().contains("Olá, Ana!"), "usa só o primeiro nome");
        assertTrue(m.texto().contains("24 horas"));
    }

    @Test
    void redefinicaoAvisaAValidadeDe30Minutos() {
        MensagemEmail m = modelos.montar(new EmailDeContaSolicitado(TipoEmailConta.REDEFINIR_SENHA,
                "ana@teste.com", "Ana", "COD"));
        assertTrue(m.texto().contains("https://logicup.dev.br/redefinir-senha#COD"));
        assertTrue(m.texto().contains("30 minutos"));
    }

    @Test
    void nomeComHtmlNaoViraCodigoNoEmail() {
        MensagemEmail m = modelos.montar(new EmailDeContaSolicitado(TipoEmailConta.BOAS_VINDAS,
                "x@teste.com", "<a href=\"https://golpe.com\">Clique</a>", null));
        assertFalse(m.html().contains("<a href=\"https://golpe.com\">"), "o nome tem que sair escapado");
        assertTrue(m.html().contains("&lt;a"));
        assertTrue(m.html().contains("https://logicup.dev.br/trilha"));
    }

    @Test
    void logsNaoMostramOCodigoNemOEmailCompleto() {
        EmailDeContaSolicitado pedido = new EmailDeContaSolicitado(TipoEmailConta.CONFIRMAR_EMAIL,
                "maria.silva@gmail.com", "Maria", "SEGREDO");
        assertFalse(pedido.toString().contains("SEGREDO"));
        assertTrue(pedido.toString().contains("m***@gmail.com"));
        assertEquals("***", EnviadorDeEmail.mascarar("semarroba"));
    }

    @Test
    void falhaNoEnvioNaoDerrubaQuemPediu() {
        EnviadorDeEmail quebrado = m -> { throw new IllegalStateException("Brevo fora do ar"); };
        EnvioDeEmailListener listener = new EnvioDeEmailListener(quebrado, modelos, Runnable::run,
                new CotaDiariaDeEmails(java.time.Clock.systemDefaultZone(), 300, 270, 240), "");
        assertDoesNotThrow(() -> listener.aoSolicitarEmail(new EmailDeContaSolicitado(
                TipoEmailConta.CONFIRMAR_EMAIL, "a@teste.com", "A", "C")));
    }
}
