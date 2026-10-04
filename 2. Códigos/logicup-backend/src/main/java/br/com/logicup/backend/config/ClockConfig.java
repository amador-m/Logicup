package br.com.logicup.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * O "relógio" do sistema. Define o que é "hoje" para o streak (PR6) no fuso de São Paulo:
 * sem isso, o servidor poderia usar UTC e o dia "viraria" às 21h.
 * Por ser um bean, os testes podem trocar por um relógio fixo.
 */
@Configuration
public class ClockConfig {

    public static final ZoneId FUSO_LOGICUP = ZoneId.of("America/Sao_Paulo");

    @Bean
    public Clock clock() {
        return Clock.system(FUSO_LOGICUP);
    }
}
