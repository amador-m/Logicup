package br.com.logicup.backend.controller;

import br.com.logicup.backend.email.EmailDeContaSolicitado;
import br.com.logicup.backend.email.TipoEmailConta;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.security.TokenService;
import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Sprint 10: ajudantes para os testes depois da confirmação de e-mail.
 *
 * Por que olhar EVENTOS (ApplicationEvents) e não a "caixa de e-mails"?
 * Os testes de integração são @Transactional (tudo é desfeito no final, com rollback). O e-mail só sai
 * DEPOIS do commit (@TransactionalEventListener AFTER_COMMIT), e nos testes o commit nunca acontece.
 * Então o teste lê o pedido de e-mail no momento em que ele é publicado, com @RecordApplicationEvents.
 * O código do link está no evento (no banco só existe o hash, de propósito).
 */
final class ContasDeTeste {

    private ContasDeTeste() {
    }

    /** Conta criada direto pelo repositório, como se já tivesse confirmado o e-mail. */
    static <T extends Usuario> T confirmada(T usuario) {
        usuario.confirmarEmail(LocalDateTime.now());
        return usuario;
    }

    /** Os pedidos de e-mail de um tipo, para um endereço, na ordem em que foram publicados. */
    static List<EmailDeContaSolicitado> pedidos(ApplicationEvents eventos, TipoEmailConta tipo, String para) {
        return eventos.stream(EmailDeContaSolicitado.class)
                .filter(e -> e.tipo() == tipo && e.para().equals(para))
                .toList();
    }

    /** O código do ÚLTIMO link daquele tipo enviado para aquele endereço. */
    static String ultimoCodigo(ApplicationEvents eventos, TipoEmailConta tipo, String para) {
        List<EmailDeContaSolicitado> lista = pedidos(eventos, tipo, para);
        assertFalse(lista.isEmpty(), "esperava um e-mail " + tipo + " para " + para);
        return lista.getLast().codigo();
    }

    /** Abre o link de confirmação (como o React faz) e devolve a resposta. */
    static MvcResult abrirLinkDeConfirmacao(MockMvc mockMvc, String codigo) throws Exception {
        return mockMvc.perform(post("/api/auth/confirmar-email").with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"" + codigo + "\"}"))
                .andReturn();
    }

    /** Confirma o e-mail de quem acabou de se cadastrar e devolve o cookie de login. */
    static Cookie confirmarCadastro(MockMvc mockMvc, ApplicationEvents eventos, String email) throws Exception {
        MvcResult resultado = abrirLinkDeConfirmacao(mockMvc,
                ultimoCodigo(eventos, TipoEmailConta.CONFIRMAR_EMAIL, email));
        assertEquals(200, resultado.getResponse().getStatus(), resultado.getResponse().getContentAsString());
        Cookie cookie = resultado.getResponse().getCookie(TokenService.NOME_COOKIE);
        assertNotNull(cookie, "confirmar o e-mail deve deixar a pessoa logada");
        return cookie;
    }
}
