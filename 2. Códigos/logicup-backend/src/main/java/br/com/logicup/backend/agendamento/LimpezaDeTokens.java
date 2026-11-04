package br.com.logicup.backend.agendamento;

import br.com.logicup.backend.service.TokenUsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sprint 10: uma vez por dia, apaga os códigos de link de e-mail vencidos há mais de 30 dias
 * (Política de Privacidade v0.4, "Resumo do tratamento").
 *
 * cron = "segundo minuto hora dia mês dia-da-semana": "0 30 4 * * *" = todo dia às 04:30,
 * no fuso de São Paulo (zone). É de madrugada, depois do backup das 03:15, quando quase ninguém usa o site.
 * Se o servidor estiver desligado nesse horário, a limpeza só acontece no dia seguinte (sem problema:
 * os códigos vencidos já não funcionam; apagar é só para não guardar dados sem necessidade).
 */
@Component
public class LimpezaDeTokens {

    private static final Logger log = LoggerFactory.getLogger(LimpezaDeTokens.class);

    private final TokenUsuarioService tokens;

    public LimpezaDeTokens(TokenUsuarioService tokens) {
        this.tokens = tokens;
    }

    @Scheduled(cron = "${logicup.tokens.limpeza-cron:0 30 4 * * *}", zone = "America/Sao_Paulo")
    public void executar() {
        int apagados = tokens.apagarAntigos();
        log.info("Limpeza diária: {} código(s) de link vencido(s) há mais de 30 dias apagado(s)", apagados);
    }
}
