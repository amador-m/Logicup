package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.Aluno;

import java.time.LocalDate;

/** T4: XP, nível e streak do aluno (vai dentro da trilha e do resultado de um envio). */
public record ResumoAlunoResponse(int xp, int nivel, int streak) {

    public static ResumoAlunoResponse de(Aluno aluno, LocalDate hoje) {
        return new ResumoAlunoResponse(aluno.getPontuacaoAcumulada(), aluno.getNivelAtual(),
                aluno.getStreakVigente(hoje));
    }
}
