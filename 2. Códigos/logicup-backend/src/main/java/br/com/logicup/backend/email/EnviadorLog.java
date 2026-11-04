package br.com.logicup.backend.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Sprint 10: "envia" o e-mail escrevendo-o no console. É o padrão em DESENVOLVIMENTO (logicup.email.modo=log):
 * no seu PC, o cadastro funciona sem servidor de e-mail, e o link de confirmação aparece no log do IntelliJ
 * para você copiar e colar no navegador.
 *
 * NUNCA use em produção: o log mostraria links secretos. O perfil prod define logicup.email.modo=smtp.
 *
 * @ConditionalOnProperty: o Spring só cria este bean quando a propriedade tem o valor indicado
 * (matchIfMissing = true: também quando a propriedade nem existe).
 */
@Component
@ConditionalOnProperty(name = "logicup.email.modo", havingValue = "log", matchIfMissing = true)
public class EnviadorLog implements EnviadorDeEmail {

    private static final Logger log = LoggerFactory.getLogger(EnviadorLog.class);

    @Override
    public void enviar(MensagemEmail mensagem) {
        log.info("""

                ======== E-MAIL (modo log: NADA foi enviado de verdade) ========
                Para:    {}
                Assunto: {}
                {}
                ================================================================""",
                mensagem.para(), mensagem.assunto(), mensagem.texto());
    }
}
