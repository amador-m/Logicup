package br.com.logicup.backend.model;

/**
 * Sprint 9 (decisão A1): INATIVO virou dois estados, porque as regras são diferentes:
 *  ATIVO    = está na turma (no máximo UMA matrícula ATIVO por aluno — R5);
 *  SAIU     = o próprio aluno saiu; pode voltar com o código (R6);
 *  REMOVIDO = o professor removeu; NÃO volta pelo código (R7).
 */
public enum StatusMatricula {
    ATIVO, SAIU, REMOVIDO
}
