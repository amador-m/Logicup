package br.com.logicup.backend.email;

/**
 * Sprint 10: "envie este e-mail". É uma INTERFACE: o resto do sistema não sabe COMO o e-mail sai.
 *
 * Implementações (escolhidas pela propriedade logicup.email.modo):
 *  - EnviadorSmtp (modo "smtp", produção): envia de verdade pela Brevo, pelo protocolo SMTP;
 *  - EnviadorLog  (modo "log", padrão em desenvolvimento): só escreve o e-mail no console, para
 *    testar o cadastro no seu PC sem enviar nada (o link aparece no log do IntelliJ).
 *
 * É o mesmo padrão do ExecutorDeCodigo (Piston ou desligado): trocar de provedor de e-mail no futuro
 * é escrever OUTRA implementação, sem mexer no AuthService. Isso é o "D" do SOLID (inversão de dependência):
 * as regras de negócio dependem da abstração, não do detalhe técnico.
 */
public interface EnviadorDeEmail {

    /** Envia a mensagem. Pode lançar exceção (servidor fora do ar, chave errada); quem chama decide o que fazer. */
    void enviar(MensagemEmail mensagem);

    /** "maria.silva@gmail.com" -> "m***@gmail.com": para logs (o endereço completo é dado pessoal). */
    static String mascarar(String email) {
        if (email == null) return "?";
        int arroba = email.indexOf('@');
        if (arroba <= 0) return "***";
        return email.charAt(0) + "***" + email.substring(arroba);
    }
}
