package br.com.logicup.backend.dto;

/**
 * Resumo de um envio. proximaRodadaLiberadaId só vem preenchido quando ESTE envio liberou uma rodada nova.
 */
public record ResumoEnvioResponse(int acertos, int totalEnviado, int xpGanho,
                                  int exerciciosConcluidos, int totalExercicios,
                                  boolean rodadaAprovada, Integer proximaRodadaLiberadaId,
                                  ResumoAlunoResponse aluno) {
}
