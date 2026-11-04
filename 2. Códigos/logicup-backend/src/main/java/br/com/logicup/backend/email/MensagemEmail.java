package br.com.logicup.backend.email;

/**
 * Sprint 10: um e-mail pronto para sair. Tem DUAS versões do conteúdo:
 *  - html:  a versão bonita (cores, botão), que a maioria dos programas de e-mail mostra;
 *  - texto: a versão em texto puro, para quem desativou HTML. Ter as duas também melhora a
 *           reputação do e-mail nos filtros de spam.
 */
public record MensagemEmail(String para, String assunto, String html, String texto) {

    @Override
    public String toString() { // nunca joga o conteúdo (que tem o link secreto) num log por acidente
        return "MensagemEmail[para=" + EnviadorDeEmail.mascarar(para) + ", assunto=" + assunto + "]";
    }
}
