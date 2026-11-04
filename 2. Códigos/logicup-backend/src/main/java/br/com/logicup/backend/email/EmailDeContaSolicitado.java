package br.com.logicup.backend.email;

/**
 * Sprint 10: EVENTO "é preciso mandar um e-mail de conta para esta pessoa".
 *
 * O AuthService NÃO envia o e-mail: ele só anuncia o evento (ApplicationEventPublisher.publishEvent).
 * Quem envia é o EnvioDeEmailListener, DEPOIS que a transação do banco termina com sucesso. Vantagens:
 *  - se o cadastro falhar e for desfeito (rollback), nenhum e-mail sai para uma conta que não existe;
 *  - o envio (1 a 2 s na Brevo) acontece em outra thread: a resposta do cadastro não espera por ele;
 *  - se a Brevo estiver fora do ar, o cadastro continua valendo (a pessoa pede o reenvio depois).
 *
 * O AuthService também não sabe montar links: ele manda só o CÓDIGO; quem transforma em
 * "https://logicup.dev.br/confirmar-email#código" é o ModelosEmail.
 *
 * @param codigo o código de uso único do link (null nas boas-vindas, que não têm link secreto)
 */
public record EmailDeContaSolicitado(TipoEmailConta tipo, String para, String nome, String codigo) {

    @Override
    public String toString() { // o código é secreto: fora dos logs
        return "EmailDeContaSolicitado[tipo=" + tipo + ", para=" + EnviadorDeEmail.mascarar(para) + "]";
    }
}
