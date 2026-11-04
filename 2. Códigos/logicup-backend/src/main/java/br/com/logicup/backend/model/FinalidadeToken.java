package br.com.logicup.backend.model;

import java.time.Duration;

/**
 * Sprint 10: para que serve um código enviado por e-mail (coluna token_usuario.finalidade).
 * A validade fica junto da finalidade: quem cria o código não precisa lembrar (nem errar) o prazo.
 *
 * Por que prazos diferentes: confirmar o e-mail é de baixo risco (só libera a própria conta) e a pessoa
 * pode demorar para abrir o e-mail; trocar a senha é de alto risco (quem tem o link entra na conta),
 * então o link vale pouco tempo.
 */
public enum FinalidadeToken {
    CONFIRMAR_EMAIL(Duration.ofHours(24)),
    REDEFINIR_SENHA(Duration.ofMinutes(30));

    private final Duration validade;

    FinalidadeToken(Duration validade) {
        this.validade = validade;
    }

    public Duration getValidade() {
        return validade;
    }
}
