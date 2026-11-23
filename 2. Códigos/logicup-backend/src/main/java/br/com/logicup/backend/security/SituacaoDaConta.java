package br.com.logicup.backend.security;

import br.com.logicup.backend.config.ClockConfig;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Sprint 11: o que o ContaBloqueadaFilter consulta a cada requisição autenticada.
 *
 * @param bloqueadoEm     quando o admin bloqueou a conta (null = ativa)
 * @param senhaAlteradaEm quando a senha mudou pela última vez (null = nunca, desde a migração 15)
 */
public record SituacaoDaConta(LocalDateTime bloqueadoEm, LocalDateTime senhaAlteradaEm) {

    public boolean bloqueada() {
        return bloqueadoEm != null;
    }

    /**
     * O token foi emitido ANTES da última troca de senha? Então ele é de uma sessão antiga e deve ser recusado.
     * O banco guarda a hora local de São Paulo (como as outras datas do sistema); o "iat" do token é um
     * instante absoluto (UTC). Por isso a conversão com o fuso do LogiCup antes de comparar.
     * Comparação ESTRITA (isBefore): um token emitido no mesmo segundo da troca é o token novo, e vale.
     */
    public boolean tokenAnteriorATrocaDeSenha(Instant emitidoEm) {
        if (senhaAlteradaEm == null || emitidoEm == null) {
            return false;
        }
        return emitidoEm.isBefore(senhaAlteradaEm.atZone(ClockConfig.FUSO_LOGICUP).toInstant());
    }
}
