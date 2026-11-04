package br.com.logicup.backend.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Sprint 10: envia e-mails de verdade por SMTP (produção: logicup.email.modo=smtp).
 *
 * Quem faz o trabalho pesado é o JavaMailSender, criado pelo Spring Boot (spring-boot-starter-mail) a partir
 * das propriedades spring.mail.* (servidor smtp-relay.brevo.com, porta 587, login e chave da Brevo).
 * Porta 587 + STARTTLS: a conversa começa sem criptografia e "sobe" para TLS antes de enviar o login e a
 * mensagem, então a chave nunca trafega aberta na rede.
 *
 * A mensagem sai como "multipart/alternative": a mesma mensagem em texto puro E em HTML; o programa de e-mail
 * escolhe qual mostrar.
 */
@Component
@ConditionalOnProperty(name = "logicup.email.modo", havingValue = "smtp")
public class EnviadorSmtp implements EnviadorDeEmail {

    private final JavaMailSender mailSender;
    private final String remetente;

    public EnviadorSmtp(JavaMailSender mailSender,
                        @Value("${logicup.email.remetente}") String remetente,
                        @Value("${spring.mail.username:}") String usuario,
                        @Value("${spring.mail.password:}") String chave) {
        // falha AO SUBIR, com mensagem clara, em vez de subir "funcionando" e só descobrir no 1º cadastro
        if (usuario.isBlank() || chave.isBlank()) {
            throw new IllegalStateException("Modo de e-mail \"smtp\" sem SMTP_USUARIO/SMTP_CHAVE no .env");
        }
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    @Override
    public void enviar(MensagemEmail mensagem) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            // true = multipart (texto + HTML); UTF-8 para os acentos chegarem certos
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());
            helper.setFrom(remetente);
            helper.setTo(mensagem.para());
            helper.setSubject(mensagem.assunto());
            helper.setText(mensagem.texto(), mensagem.html()); // (texto puro, HTML)
            mailSender.send(mime);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("Falha ao enviar e-mail para " + EnviadorDeEmail.mascarar(mensagem.para()), e);
        }
    }
}
