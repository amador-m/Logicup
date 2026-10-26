package br.com.logicup.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Sprint 8: POST /api/legal/aceite. As versões que a pessoa VIU na tela.
 * O backend só aceita se forem as vigentes; o que é gravado é sempre a versão do servidor.
 */
public record AceiteRequest(
        @NotBlank(message = "Informe a versão dos Termos de Uso que você leu.") String versaoTermos,
        @NotBlank(message = "Informe a versão da Política de Privacidade que você leu.") String versaoPrivacidade) {
}
