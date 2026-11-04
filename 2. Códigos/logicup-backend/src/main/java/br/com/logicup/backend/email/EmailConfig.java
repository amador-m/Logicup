package br.com.logicup.backend.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Sprint 10: a "fila" de envio de e-mails.
 *
 * logicup.email.envio-assincrono=true (padrão): um ThreadPoolTaskExecutor com 2 threads e fila de 200.
 *   O cadastro responde na hora; o e-mail sai alguns instantes depois, em segundo plano.
 *   2 threads bastam: a Brevo grátis permite 300 e-mails por DIA, ninguém vai mandar 2 por segundo.
 * logicup.email.envio-assincrono=false (testes): Runnable::run, ou seja, roda na MESMA thread.
 *   Assim o teste sabe que, quando a requisição terminou, o e-mail já foi "enviado".
 */
@Configuration
public class EmailConfig {

    public static final String EXECUTOR_EMAILS = "executorDeEmails";

    @Bean(name = EXECUTOR_EMAILS)
    public Executor executorDeEmails(@Value("${logicup.email.envio-assincrono:true}") boolean assincrono) {
        if (!assincrono) {
            return Runnable::run;
        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("email-");
        // ao desligar o servidor (atualização), espera até 20 s para os e-mails da fila saírem
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }
}
