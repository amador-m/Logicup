package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.TipoExercicio;

import java.util.List;

/**
 * GET /api/ementa (Sprint 7): o que cada fase, rodada e questão desenvolve no aluno.
 * Só professor e admin acessam (regra no SecurityConfig).
 *
 * Três níveis, um dentro do outro:  fase  ->  rodadas  ->  questões
 *   fase    : objetivo geral   = modulo.descricao
 *   rodada  : objetivos        = "objetivos" do conteudo_teorico (os mesmos que o aluno vê na teoria)
 *   questão : habilidade       = exercicio.habilidade (null enquanto não for escrita)
 *
 * Os records internos só existem como partes desta resposta, por isso ficam aqui dentro
 * (mesma ideia do SituacaoTrilha dentro do TrilhaService).
 * De propósito NÃO há enunciado, alternativas, gabarito nem explicação: a ementa descreve
 * o que é avaliado, não entrega as respostas.
 */
public record EmentaResponse(List<FaseEmenta> fases) {

    public record FaseEmenta(Integer id, Integer ordem, String titulo, String objetivoGeral,
                             List<RodadaEmenta> rodadas) {
    }

    public record RodadaEmenta(Integer id, Integer ordem, String titulo, List<String> objetivos,
                               List<QuestaoEmenta> questoes) {
    }

    public record QuestaoEmenta(Integer id, String codigo, Integer ordem, String titulo,
                                TipoExercicio tipo, Integer pontos, String habilidade) {
    }
}
