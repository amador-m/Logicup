package br.com.logicup.backend.model;

/**
 * Rótulo PEDAGÓGICO da questão, igual aos 12 tipos do Banco de Questões.
 * Não define como a questão é corrigida — isso é papel do FormatoResposta.
 */
public enum TipoExercicio {
    VERDADEIRO_FALSO,
    MULTIPLA_ESCOLHA,
    BLOCOS,
    COMPLETAR_CODIGO,
    ENCONTRAR_ERRO,
    PREVER_SAIDA,
    ESCREVER_CODIGO,
    ASSOCIACAO,
    DESAFIO_RELAMPAGO,
    PUZZLE_LOGICO,
    ORDEM_CORRETA,
    BOSS_CHALLENGE
}