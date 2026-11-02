package br.com.logicup.backend.dto;

import br.com.logicup.backend.execucao.SituacaoExecucao;

/**
 * Sprint 9 (ADR-001): resultado do botão "Executar".
 * situacao: SUCESSO, ERRO_COMPILACAO... (o frontend escolhe a cor e o ícone por ela)
 * mensagem: explicação curta, em português, para o aluno
 * saida / erros: o que o programa escreveu (stdout / stderr)
 * dica: explicação do erro para iniciantes (ou null), ver DicasDeErro
 */
public record ExecucaoResponse(SituacaoExecucao situacao, String mensagem, String saida, String erros, Long tempoMs,
                               String dica) {}
