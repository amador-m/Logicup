package br.com.logicup.backend.service.correcao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Uma alternativa exibida ao aluno: {"id": "a", "texto": "..."} */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Alternativa(String id, String texto) {
}
