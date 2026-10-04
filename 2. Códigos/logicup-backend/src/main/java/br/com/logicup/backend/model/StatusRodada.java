package br.com.logicup.backend.model;

/**
 * Situação de uma rodada PARA UM ALUNO. Não é gravada no banco: é CALCULADA a partir do progresso
 * (assim nunca fica desatualizada em relação ao que o aluno realmente concluiu).
 */
public enum StatusRodada {
    BLOQUEADA, // a rodada anterior ainda não foi aprovada
    LIBERADA,  // pode ser feita
    APROVADA   // PR5: >= 80% dos exercícios concluídos, com o Boss concluído
}
