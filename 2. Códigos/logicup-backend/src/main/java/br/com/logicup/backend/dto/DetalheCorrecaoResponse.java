package br.com.logicup.backend.dto;

/**
 * Sprint 9 (ADR-001): o "porquê" da correção de uma questão de CÓDIGO (vai para o aluno).
 *
 * tipo:           TESTES_OK, SAIDA_DIFERENTE, ERRO_COMPILACAO, ERRO_EXECUCAO, TEMPO_ESGOTADO,
 *                 ESTRUTURA (escreveu um programa inteiro quando era só um trecho) ou
 *                 EXECUTOR_INDISPONIVEL (questão PENDENTE: não deu para executar agora)
 * mensagem:       frase principal, em português
 * casosAprovados / totalCasos: progresso nos casos de teste (ex.: 3 de 7)
 * caso, esperado, obtido: SÓ do primeiro caso que falhou (decisão 2 de 02/11: os outros continuam ocultos)
 * erros:          mensagem do compilador/da JVM, com as linhas traduzidas para as linhas do aluno
 * dica:           explicação do erro para iniciantes (ou null)
 *
 * Campos sem valor ficam null (o frontend mostra só o que vier preenchido).
 */
public record DetalheCorrecaoResponse(String tipo, String mensagem, Integer casosAprovados, Integer totalCasos,
                                      String caso, String esperado, String obtido, String erros, String dica) {
}
