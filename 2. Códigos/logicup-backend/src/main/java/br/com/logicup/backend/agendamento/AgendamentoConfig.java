package br.com.logicup.backend.agendamento;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Sprint 10: liga as tarefas agendadas (@Scheduled) do LogiCup.
 *
 * @EnableScheduling faz o Spring procurar métodos com @Scheduled e chamá-los nos horários marcados,
 * numa thread própria ("scheduling-1"), sem nenhuma requisição HTTP envolvida.
 *
 * Fica DESLIGADO (logicup.agendamentos.habilitado=false):
 *  - no perfil criar-admin: a aplicação precisa TERMINAR depois de criar o admin, e uma thread de
 *    agendamento a deixaria rodando para sempre;
 *  - nos testes: o teste chama o método da tarefa diretamente, na hora que quiser.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "logicup.agendamentos.habilitado", havingValue = "true", matchIfMissing = true)
public class AgendamentoConfig {
}
