package br.com.logicup.backend.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * Sprint 10: o TEXTO dos três e-mails do LogiCup (confirmação, nova senha e boas-vindas).
 *
 * Por que o HTML é "à moda antiga" (tabelas e estilo dentro de cada tag)?
 * Programas de e-mail (Gmail, Outlook) ignoram boa parte do CSS moderno: não carregam arquivos .css,
 * cortam <style> e não entendem flexbox/grid. Tabela + style="" é o jeito que funciona em todos.
 *
 * Segurança:
 *  - o NOME vem do cadastro (texto digitado pela pessoa) e entra no HTML ESCAPADO (HtmlUtils.htmlEscape):
 *    um nome como "<a href=...>" vira texto, e não um link falso dentro de um e-mail com a nossa marca;
 *  - o link leva o código depois do "#" (fragmento). O navegador NÃO envia o fragmento ao servidor
 *    nem ao cabeçalho Referer: o código não aparece em log de servidor nenhum. Quem lê é o React.
 */
@Component
public class ModelosEmail {

    // cores do LogiCup (as mesmas variáveis do Global.js do frontend)
    private static final String LARANJA = "#e35c33";
    private static final String ESCURO = "#040423";
    private static final String CREME = "#f1f0e2";

    private final String urlPublica;

    public ModelosEmail(@Value("${logicup.email.url-publica:http://localhost:5173}") String urlPublica) {
        // sem "/" no final, para não gerar "https://logicup.dev.br//confirmar-email"
        this.urlPublica = urlPublica.endsWith("/") ? urlPublica.substring(0, urlPublica.length() - 1) : urlPublica;
    }

    public MensagemEmail montar(EmailDeContaSolicitado pedido) {
        return switch (pedido.tipo()) {
            case CONFIRMAR_EMAIL -> confirmacao(pedido.para(), pedido.nome(), urlPublica + "/confirmar-email#" + pedido.codigo());
            case REDEFINIR_SENHA -> redefinicao(pedido.para(), pedido.nome(), urlPublica + "/redefinir-senha#" + pedido.codigo());
            case BOAS_VINDAS -> boasVindas(pedido.para(), pedido.nome(), urlPublica + "/trilha");
        };
    }

    private MensagemEmail confirmacao(String para, String nome, String link) {
        String assunto = "Confirme seu e-mail no LogiCup";
        String texto = """
                Olá, %s!

                Falta só um passo para começar a aprender Java no LogiCup: confirme seu e-mail abrindo o link abaixo.

                %s

                O link vale por 24 horas e só pode ser usado uma vez.
                Se você não criou uma conta no LogiCup, ignore este e-mail: nenhuma conta será ativada.
                """.formatted(primeiroNome(nome), link);
        String html = moldura(assunto,
                paragrafo("Olá, <strong>" + escapar(primeiroNome(nome)) + "</strong>!")
                        + paragrafo("Falta só um passo para começar a aprender Java no LogiCup: confirme seu e-mail.")
                        + botao("Confirmar meu e-mail", link)
                        + miudo("O link vale por 24 horas e só pode ser usado uma vez.")
                        + miudo("Se você não criou uma conta no LogiCup, ignore este e-mail: nenhuma conta será ativada.")
                        + linkReserva(link));
        return new MensagemEmail(para, assunto, html, texto);
    }

    private MensagemEmail redefinicao(String para, String nome, String link) {
        String assunto = "Crie uma nova senha no LogiCup";
        String texto = """
                Olá, %s!

                Recebemos um pedido para criar uma nova senha para a sua conta. Para continuar, abra o link abaixo.

                %s

                O link vale por 30 minutos e só pode ser usado uma vez.
                Se não foi você, ignore este e-mail: sua senha atual continua a mesma.
                """.formatted(primeiroNome(nome), link);
        String html = moldura(assunto,
                paragrafo("Olá, <strong>" + escapar(primeiroNome(nome)) + "</strong>!")
                        + paragrafo("Recebemos um pedido para criar uma nova senha para a sua conta.")
                        + botao("Criar nova senha", link)
                        + miudo("O link vale por 30 minutos e só pode ser usado uma vez.")
                        + miudo("Se não foi você, ignore este e-mail: sua senha atual continua a mesma.")
                        + linkReserva(link));
        return new MensagemEmail(para, assunto, html, texto);
    }

    private MensagemEmail boasVindas(String para, String nome, String link) {
        String assunto = "Boas-vindas ao LogiCup!";
        String texto = """
                Olá, %s!

                Seu e-mail foi confirmado e sua conta está pronta. Que bom ter você por aqui!

                No LogiCup você aprende Java por fases: cada rodada tem teoria curta, exemplos e exercícios
                com correção na hora. Acertando, você ganha XP, sobe de nível e desbloqueia conquistas.

                Para começar, abra a sua trilha: %s

                Bons estudos!
                Equipe LogiCup
                """.formatted(primeiroNome(nome), link);
        String html = moldura(assunto,
                paragrafo("Olá, <strong>" + escapar(primeiroNome(nome)) + "</strong>!")
                        + paragrafo("Seu e-mail foi confirmado e sua conta está pronta. Que bom ter você por aqui!")
                        + paragrafo("No LogiCup você aprende Java por fases: cada rodada tem teoria curta, exemplos e "
                        + "exercícios com correção na hora. Acertando, você ganha XP, sobe de nível e desbloqueia conquistas.")
                        + botao("Começar minha trilha", link)
                        + paragrafo("Bons estudos!<br>Equipe LogiCup"));
        return new MensagemEmail(para, assunto, html, texto);
    }

    /** Sprint 10: aviso interno para a equipe (não vai para alunos). Texto simples, sem link secreto. */
    public MensagemEmail alertaDeCota(String para, int enviados, int limite) {
        String assunto = "[LogiCup] Cota de e-mails do dia quase no fim (" + enviados + " de " + limite + ")";
        String texto = """
                O LogiCup já enviou %d e-mails hoje. O plano grátis da Brevo permite %d por dia.

                O que acontece agora:
                - perto do limite, os e-mails de boas-vindas deixam de sair (os de confirmação têm prioridade);
                - no limite, nenhum e-mail sai até a meia-noite (horário de Brasília). Quem se cadastrar
                  nesse intervalo pode pedir o link de novo no dia seguinte, pela tela de login.

                Se isso acontecer com frequência, veja o painel da Brevo (estatísticas de e-mail transacional)
                e avalie um plano pago, ou investigue se alguém está abusando do cadastro.
                Este aviso é enviado no máximo uma vez por dia.
                """.formatted(enviados, limite);
        String html = moldura(assunto, "<pre style=\"white-space:pre-wrap;font-family:Arial,sans-serif;font-size:14px;\">"
                + escapar(texto) + "</pre>");
        return new MensagemEmail(para, assunto, html, texto);
    }

    // ---------------- pedaços de HTML ----------------

    private String moldura(String titulo, String conteudo) {
        return """
                <!doctype html>
                <html lang="pt-BR"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width">
                <title>%s</title></head>
                <body style="margin:0;padding:0;background:%s;">
                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:%s;">
                <tr><td align="center" style="padding:32px 16px;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"
                         style="max-width:520px;background:#ffffff;border-radius:16px;font-family:Poppins,Arial,sans-serif;color:%s;">
                    <tr><td align="center" style="padding:28px 24px 8px;">
                      <img src="%s/icon-192.png" width="72" height="72" alt="LogiCup" style="display:block;border:0;">
                    </td></tr>
                    <tr><td style="padding:8px 32px 28px;font-size:16px;line-height:1.6;">%s</td></tr>
                  </table>
                  <p style="font-family:Arial,sans-serif;font-size:12px;color:#6b6b80;margin:16px 0 0;">
                    LogiCup — plataforma de ensino de Java · este é um e-mail automático, não responda.
                  </p>
                </td></tr></table>
                </body></html>
                """.formatted(escapar(titulo), CREME, CREME, ESCURO, urlPublica, conteudo);
    }

    private static String paragrafo(String html) {
        return "<p style=\"margin:0 0 16px;\">" + html + "</p>";
    }

    private static String miudo(String texto) {
        return "<p style=\"margin:0 0 8px;font-size:13px;color:#555566;\">" + escapar(texto) + "</p>";
    }

    private static String botao(String texto, String link) {
        return "<table role=\"presentation\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin:8px 0 24px;\"><tr>"
                + "<td style=\"background:" + LARANJA + ";border-radius:10px;\">"
                + "<a href=\"" + escapar(link) + "\" style=\"display:inline-block;padding:14px 28px;color:#ffffff;"
                + "font-weight:bold;text-decoration:none;font-size:16px;\">" + escapar(texto) + "</a>"
                + "</td></tr></table>";
    }

    /** Para quem não consegue clicar no botão (alguns programas bloqueiam): o endereço por extenso. */
    private static String linkReserva(String link) {
        return "<p style=\"margin:16px 0 0;font-size:12px;color:#555566;word-break:break-all;\">"
                + "Se o botão não funcionar, copie e cole no navegador:<br>" + escapar(link) + "</p>";
    }

    private static String escapar(String texto) {
        return HtmlUtils.htmlEscape(texto == null ? "" : texto, "UTF-8");
    }

    /** "Maria Eduarda Amador" -> "Maria": o e-mail fica mais pessoal sem expor o nome completo. */
    static String primeiroNome(String nome) {
        if (nome == null || nome.isBlank()) return "estudante";
        return nome.trim().split("\\s+")[0];
    }
}
