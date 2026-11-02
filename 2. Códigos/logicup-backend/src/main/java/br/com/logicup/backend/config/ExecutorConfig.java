package br.com.logicup.backend.config;

import br.com.logicup.backend.execucao.ExecutorDeCodigo;
import br.com.logicup.backend.execucao.ExecutorDesligado;
import br.com.logicup.backend.execucao.ExecutorPiston;
import br.com.logicup.backend.execucao.ParametrosExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

/**
 * Sprint 9 (ADR-001): decide QUAL ExecutorDeCodigo o Spring vai injetar.
 *
 *   logicup.executor.habilitado=true  -> ExecutorPiston (precisa do Piston rodando)
 *   logicup.executor.habilitado=false -> ExecutorDesligado (o site funciona; "Executar" avisa que está indisponível)
 *
 * Quem usa o executor (Services) recebe a INTERFACE pelo construtor e não sabe qual das duas é.
 * Os valores ficam no application.properties (bloco logicup.executor.*).
 */
@Configuration
public class ExecutorConfig {

    private static final Logger log = LoggerFactory.getLogger(ExecutorConfig.class);

    @Bean
    public ExecutorDeCodigo executorDeCodigo(
            JsonMapper jsonMapper,
            @Value("${logicup.executor.habilitado:false}") boolean habilitado,
            @Value("${logicup.executor.url:http://localhost:2000}") String url,
            @Value("${logicup.executor.versao-java:15.0.2}") String versaoJava,
            @Value("${logicup.executor.tempo-execucao:6s}") Duration tempoExecucao,
            @Value("${logicup.executor.tempo-cpu:5s}") Duration tempoCpu,
            @Value("${logicup.executor.memoria-mb:256}") long memoriaMb,
            @Value("${logicup.executor.simultaneas:3}") int simultaneas,
            @Value("${logicup.executor.espera-na-fila:10s}") Duration esperaNaFila) {

        if (!habilitado) {
            log.info("Executor de código DESLIGADO (logicup.executor.habilitado=false)");
            return new ExecutorDesligado();
        }
        log.info("Executor de código: Piston em {} (Java {}, {} simultâneas)", url, versaoJava, simultaneas);
        return new ExecutorPiston(new ParametrosExecutor(url, versaoJava, tempoExecucao, tempoCpu,
                memoriaMb * 1024 * 1024, simultaneas, esperaNaFila), jsonMapper);
    }
}
