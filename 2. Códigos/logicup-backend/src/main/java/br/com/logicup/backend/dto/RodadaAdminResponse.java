package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.TipoExercicio;
import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.List;

/**
 * Sprint 11: uma rodada com tudo o que o admin pode EDITAR: o título, a teoria (JSON, enviado como está)
 * e os textos de cada exercício. Gabarito e alternativas NÃO vêm, de propósito (não são editáveis aqui).
 */
public record RodadaAdminResponse(Integer id, Integer faseOrdem, String faseTitulo, Integer ordem, String titulo,
                                  @JsonRawValue String conteudo, List<Exercicio> exercicios) {

    public record Exercicio(Integer id, String codigo, String titulo, TipoExercicio tipo, String enunciado,
                            String explicacao) {
    }
}
