package br.com.logicup.backend.controller;

import br.com.logicup.backend.google.GoogleCadastroPendente;
import br.com.logicup.backend.google.GoogleLoginConfig;
import br.com.logicup.backend.google.GoogleLoginService;
import br.com.logicup.backend.google.IdentidadeGoogle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 10: com a chave do Google configurada (aqui, uma chave de mentira), a ida para o Google funciona.
 * Não chega a falar com o Google: confere só o redirecionamento que o Spring monta.
 */
@SpringBootTest(properties = {
        "logicup.google.client-id=123-teste.apps.googleusercontent.com",
        "logicup.google.client-secret=GOCSPX-segredo-de-teste-123456789"
})
@AutoConfigureMockMvc
class GoogleLigadoIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void botaoDoGoogleAparece() throws Exception {
        mockMvc.perform(get("/api/auth/provedores")).andExpect(jsonPath("$.google").value(true));
    }

    @Test
    void idaParaOGoogleLevaStateNonceEOEnderecoDeVolta() throws Exception {
        MvcResult ida = mockMvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", startsWith("https://accounts.google.com/o/oauth2/v2/auth")))
                .andReturn();
        String destino = ida.getResponse().getHeader("Location");
        assertTrue(destino.contains("client_id=123-teste.apps.googleusercontent.com"));
        assertTrue(destino.contains("state="), "o state protege contra login forjado");
        assertTrue(destino.contains("nonce="), "o nonce protege contra reaproveitar um ID token");
        assertTrue(destino.contains("scope=openid%20email%20profile") || destino.contains("scope=openid+email+profile"));
        assertTrue(destino.contains("redirect_uri=http://localhost/login/oauth2/code/google")
                || destino.contains("redirect_uri=http%3A%2F%2Flocalhost%2Flogin%2Foauth2%2Fcode%2Fgoogle"), destino);
        assertTrue(ida.getRequest().getSession(false) != null, "o state fica na sessão só durante a ida e volta");
    }

    @Test
    void voltaSemStateValidoVoltaParaOLoginComAviso() throws Exception {
        mockMvc.perform(get("/login/oauth2/code/google").param("code", "x").param("state", "inventado"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/login?erro=google"));
    }

    @Test
    void oRestoDaApiContinuaSemSessao() throws Exception {
        MvcResult resposta = mockMvc.perform(get("/api/auth/csrf")).andReturn();
        assertTrue(resposta.getRequest().getSession(false) == null);
    }
}
