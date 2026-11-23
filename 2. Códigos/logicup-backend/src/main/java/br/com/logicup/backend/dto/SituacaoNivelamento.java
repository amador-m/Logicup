package br.com.logicup.backend.dto;

/** Sprint 11: em que pé está o nivelamento do aluno. */
public enum SituacaoNivelamento {
    /** Primeiro acesso: ainda não escolheu e ainda não respondeu nada na trilha -> oferecer o teste. */
    PENDENTE,
    /** Fez o teste (o resultado vem junto). */
    FEITO,
    /** Escolheu começar do zero. */
    NAO_QUIS,
    /** Já começou a trilha antes de existir o nivelamento (ou sem passar por ele): não é mais oferecido. */
    NAO_DISPONIVEL
}
