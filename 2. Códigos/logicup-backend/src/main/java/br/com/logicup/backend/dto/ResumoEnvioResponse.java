package br.com.logicup.backend.dto;

/**
 * Resumo de um envio. proximaRodadaLiberadaId só vem preenchido quando ESTE envio liberou uma rodada nova.
 * Sprint 9 (ADR-001): pendentes = questões de código que não puderam ser executadas neste envio
 * (não contam nem como acerto nem como erro).
 */
public record ResumoEnvioResponse(int acertos, int totalEnviado, int xpGanho,
                                  int exerciciosConcluidos, int totalExercicios,
                                  boolean rodadaAprovada, Integer proximaRodadaLiberadaId,
                                  ResumoAlunoResponse aluno, int pendentes) {
}
