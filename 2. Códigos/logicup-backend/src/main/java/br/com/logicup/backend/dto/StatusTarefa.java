package br.com.logicup.backend.dto;

/**
 * Sprint 9 (R11): situação de uma tarefa para UM aluno, derivada do status da rodada na trilha.
 * Não é guardada no banco: muda sozinha quando o aluno aprova a rodada.
 */
public enum StatusTarefa {
    PENDENTE,   // rodada liberada, ainda não aprovada
    CONCLUIDA,  // rodada aprovada
    BLOQUEADA   // o aluno ainda não chegou a essa rodada na trilha (a tarefa não muda a ordem — decisão A2)
}
