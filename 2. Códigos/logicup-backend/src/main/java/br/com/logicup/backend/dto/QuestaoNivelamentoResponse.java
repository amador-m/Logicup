package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.QuestaoNivelamento;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * Sprint 11: uma questão do nivelamento como o ALUNO a vê. Não tem a resposta certa nem a explicação,
 * de propósito: se não está no DTO, não tem como vazar na API (mesma ideia do ExercicioResponse).
 */
public record QuestaoNivelamentoResponse(Integer id, Integer fase, String enunciado, String codigo,
                                         @JsonRawValue String alternativas) {

    public static QuestaoNivelamentoResponse de(QuestaoNivelamento questao) {
        return new QuestaoNivelamentoResponse(questao.getId(), questao.getFase(), questao.getEnunciado(),
                questao.getCodigo(), questao.getAlternativasJson());
    }
}
