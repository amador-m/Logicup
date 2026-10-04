package br.com.logicup.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * D4: o frontend envia as respostas da rodada numa única requisição.
 * T2: pode ser só uma parte dos exercícios (ex.: refazer apenas os que errou).
 * @Valid na lista faz o Spring validar também cada item.
 */
public record EnvioRespostasRequest(
        @NotEmpty(message = "Envie pelo menos uma resposta.")
        List<@Valid RespostaExercicioRequest> respostas
) {
}
