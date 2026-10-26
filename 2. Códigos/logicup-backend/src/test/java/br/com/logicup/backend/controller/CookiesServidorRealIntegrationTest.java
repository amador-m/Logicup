package br.com.logicup.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tarefa 6, etapa 4: cookies conferidos no SERVIDOR DE VERDADE (Tomcat embutido, porta HTTP real),
 * com o cliente HTTP do próprio Java. Nada de MockMvc aqui.
 *
 * Por que existe: o cookie CSRF é escrito pelo Spring Security como objeto Cookie do servlet, e quem
 * o transforma no texto "Set-Cookie" é o servidor. O MockMvc não inclui o atributo SameSite nesse
 * texto; este teste mostra o que o Tomcat realmente manda ao navegador.
 *
 * Só usa rotas que NÃO gravam no banco (/csrf e logout): aqui não há @Transactional para desfazer,
 * porque a requisição roda em outra thread, no servidor.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CookiesServidorRealIntegrationTest {

    @Value("${local.server.port}")
    private int porta;

    private final HttpClient cliente = HttpClient.newHttpClient(); // sem CookieHandler: os Set-Cookie chegam intactos

    private URI url(String caminho) {
        return URI.create("http://localhost:" + porta + caminho);
    }

    /** A linha Set-Cookie (texto bruto) de um cookie, ignorando as remoções (Max-Age=0). */
    private static String setCookie(HttpResponse<?> resposta, String nome, boolean remocao) {
        return resposta.headers().allValues("Set-Cookie").stream()
                .filter(linha -> linha.startsWith(nome + "="))
                .filter(linha -> linha.toLowerCase(Locale.ROOT).contains("max-age=0") == remocao)
                .findFirst()
                .orElseThrow(() -> new AssertionError("sem Set-Cookie de " + nome + " em "
                        + resposta.headers().allValues("Set-Cookie")));
    }

    private static List<String> atributos(String linha) {
        return List.of(linha.split(";")).stream().skip(1).map(a -> a.trim().toLowerCase(Locale.ROOT)).toList();
    }

    private static String valor(String linha) {
        return linha.substring(linha.indexOf('=') + 1, linha.contains(";") ? linha.indexOf(';') : linha.length());
    }

    /** __Host- (Secure, Path=/, sem Domain) + SameSite=Strict, conferidos no texto que o navegador recebe. */
    private static void cookieHostStrict(String linha) {
        List<String> atributos = atributos(linha);
        assertTrue(atributos.contains("secure"), "Secure ausente: " + linha);
        assertTrue(atributos.contains("path=/"), "Path=/ ausente: " + linha);
        assertTrue(atributos.stream().noneMatch(a -> a.startsWith("domain")), "Domain proibido: " + linha);
        assertTrue(atributos.contains("samesite=strict"), "SameSite=Strict ausente: " + linha);
    }

    @Test
    void cookieCsrfNoTomcatTemSecurePathESameSiteStrict() throws Exception {
        HttpResponse<Void> resposta = cliente.send(HttpRequest.newBuilder(url("/api/auth/csrf")).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        assertEquals(204, resposta.statusCode());

        String csrf = setCookie(resposta, "__Host-XSRF-TOKEN", false);
        cookieHostStrict(csrf);
        assertFalse(atributos(csrf).contains("httponly"), "o React precisa ler o cookie CSRF: " + csrf);
    }

    @Test
    void logoutNoTomcatApagaOLoginETrocaOCsrfComTodosOsAtributos() throws Exception {
        HttpResponse<Void> pedido = cliente.send(HttpRequest.newBuilder(url("/api/auth/csrf")).GET().build(),
                HttpResponse.BodyHandlers.discarding());
        String tokenAntes = valor(setCookie(pedido, "__Host-XSRF-TOKEN", false));

        HttpResponse<Void> logout = cliente.send(HttpRequest.newBuilder(url("/api/auth/logout"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .header("Cookie", "__Host-XSRF-TOKEN=" + tokenAntes)
                        .header("X-XSRF-TOKEN", tokenAntes)
                        .build(),
                HttpResponse.BodyHandlers.discarding());
        assertEquals(204, logout.statusCode());

        // remoção do login: Max-Age=0 com os MESMOS atributos obrigatórios, senão o navegador não apaga
        String remocaoLogin = setCookie(logout, "__Host-LOGICUP_TOKEN", true);
        cookieHostStrict(remocaoLogin);
        assertTrue(atributos(remocaoLogin).contains("httponly"), remocaoLogin);

        // token CSRF trocado (não apagado), com todos os atributos
        String csrfNovo = setCookie(logout, "__Host-XSRF-TOKEN", false);
        cookieHostStrict(csrfNovo);
        assertNotEquals(tokenAntes, valor(csrfNovo), "o logout deveria trocar o token CSRF");
    }
}
