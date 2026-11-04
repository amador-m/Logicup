package br.com.logicup.backend.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * Sprint 10 (decisão E1-A): ESCUTA o evento EmailDeContaSolicitado e envia o e-mail.
 *
 * @TransactionalEventListener(phase = AFTER_COMMIT): o Spring guarda o evento e só chama este método
 * DEPOIS que a transação do AuthService for gravada no banco (commit). Se der rollback, o método nem roda.
 * fallbackExecution = true: se o evento for publicado FORA de uma transação, roda na hora mesmo assim.
 *
 * O envio vai para o "executorDeEmails" (outra thread), então a resposta HTTP não espera a Brevo.
 * Nos testes, esse executor roda na mesma thread (EmailConfig), para os testes serem previsíveis.
 *
 * Falha no envio NÃO derruba nada: vira um aviso no log (com o e-mail mascarado) e a pessoa pode
 * pedir o reenvio pela tela de login. Isso é aceitável para um TCC; um sistema maior guardaria os
 * envios numa fila no banco para tentar de novo sozinho (anotado como melhoria futura).
 */
@Component
public class EnvioDeEmailListener {

    private static final Logger log = LoggerFactory.getLogger(EnvioDeEmailListener.class);

    private final EnviadorDeEmail enviador;
    private final ModelosEmail modelos;
    private final Executor executor;
    private final CotaDiariaDeEmails cota;   // Sprint 10: até 300 por dia (plano grátis da Brevo)
    private final String alertaPara;          // quem recebe o aviso de "cota quase no fim"

    public EnvioDeEmailListener(EnviadorDeEmail enviador, ModelosEmail modelos,
                                @Qualifier(EmailConfig.EXECUTOR_EMAILS) Executor executor,
                                CotaDiariaDeEmails cota,
                                @Value("${logicup.email.cota.alerta-para:}") String alertaPara) {
        this.enviador = enviador;
        this.modelos = modelos;
        this.executor = executor;
        this.cota = cota;
        this.alertaPara = alertaPara;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void aoSolicitarEmail(EmailDeContaSolicitado pedido) {
        try {
            executor.execute(() -> enviar(pedido));
        } catch (RejectedExecutionException e) { // fila cheia (centenas de e-mails parados): não trava o cadastro
            log.warn("Fila de e-mails cheia; não foi possível enviar {} para {}", pedido.tipo(),
                    EnviadorDeEmail.mascarar(pedido.para()));
        }
    }

    private void enviar(EmailDeContaSolicitado pedido) {
        if (!cota.podeEnviar(pedido.tipo())) {
            // "COTA_EMAIL" é uma palavra fixa: dá para procurar no log (grep COTA_EMAIL)
            log.warn("COTA_EMAIL esgotada para {} ({} de {} hoje): e-mail para {} NÃO enviado", pedido.tipo(),
                    cota.enviadosHoje(), cota.limite(), EnviadorDeEmail.mascarar(pedido.para()));
            return;
        }
        try {
            enviador.enviar(modelos.montar(pedido));
            log.info("E-mail {} enviado para {}", pedido.tipo(), EnviadorDeEmail.mascarar(pedido.para()));
        } catch (RuntimeException e) {
            log.warn("Falha ao enviar e-mail {} para {}: {}", pedido.tipo(),
                    EnviadorDeEmail.mascarar(pedido.para()), e.getMessage());
            return;
        }
        if (cota.registrarEnvio()) {
            avisarCotaQuaseNoFim();
        }
    }

    /** Uma vez por dia, quando 80% da cota foi usada: um aviso no log e um e-mail para a equipe. */
    private void avisarCotaQuaseNoFim() {
        log.warn("COTA_EMAIL perto do limite: {} de {} e-mails enviados hoje", cota.enviadosHoje(), cota.limite());
        if (alertaPara.isBlank()) {
            return;
        }
        try {
            enviador.enviar(modelos.alertaDeCota(alertaPara, cota.enviadosHoje(), cota.limite()));
            cota.registrarEnvio(); // o próprio aviso também gasta 1 da cota da Brevo
        } catch (RuntimeException e) {
            log.warn("Falha ao enviar o aviso de cota: {}", e.getMessage());
        }
    }
}
