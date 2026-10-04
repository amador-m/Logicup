package br.com.logicup.backend.dto;

import java.util.List;

/** GET /api/trilha. "aluno" é null para PROFESSOR/ADMIN (eles não fazem a trilha). */
public record TrilhaResponse(ResumoAlunoResponse aluno, List<FaseResponse> fases) {
}
