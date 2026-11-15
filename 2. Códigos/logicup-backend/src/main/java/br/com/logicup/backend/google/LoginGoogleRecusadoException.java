package br.com.logicup.backend.google;

/**
 * Sprint 10: o Google confirmou quem é a pessoa, mas o LogiCup não pode deixá-la entrar.
 * O "motivo" vira o parâmetro ?erro= da tela de login, que mostra a mensagem certa.
 */
public class LoginGoogleRecusadoException extends RuntimeException {

    public enum Motivo {
        /** o Google não garante que a pessoa é dona do e-mail */
        EMAIL_NAO_VERIFICADO,
        /** o e-mail já tem conta no LogiCup ligada a OUTRA conta Google */
        CONTA_GOOGLE_DIFERENTE
    }

    private final Motivo motivo;

    public LoginGoogleRecusadoException(Motivo motivo) {
        super(motivo.name());
        this.motivo = motivo;
    }

    public Motivo getMotivo() { return motivo; }
}
