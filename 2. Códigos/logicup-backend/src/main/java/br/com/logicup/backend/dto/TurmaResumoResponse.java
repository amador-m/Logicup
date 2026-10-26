package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.Turma;

/** Sprint 9: uma turma na lista do professor (e a resposta de criar, renomear e gerar novo código). */
public record TurmaResumoResponse(Integer id, String nome, String codigo, long alunosAtivos) {

    public static TurmaResumoResponse de(Turma turma, long alunosAtivos) {
        return new TurmaResumoResponse(turma.getId(), turma.getNomeTurma(), turma.getCodigoConvite(), alunosAtivos);
    }
}
