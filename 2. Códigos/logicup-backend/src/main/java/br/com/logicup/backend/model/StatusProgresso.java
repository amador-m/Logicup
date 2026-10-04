package br.com.logicup.backend.model;

/**
 *  EM_ANDAMENTO = o aluno já respondeu, mas ainda não acertou.
 *  CONCLUIDO    = acertou pelo menos uma vez. Nunca volta para EM_ANDAMENTO.
 */
public enum StatusProgresso {
    EM_ANDAMENTO, CONCLUIDO
}