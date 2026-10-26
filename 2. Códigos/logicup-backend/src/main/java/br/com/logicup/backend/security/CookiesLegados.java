package br.com.logicup.backend.security;

import org.springframework.http.ResponseCookie;

import java.util.List;

/**
 * Tarefa 6 (TRANSIÇÃO): apaga os cookies com os nomes ANTIGOS, de antes do prefixo __Host-.
 *
 * Quem fez login antes da mudança ainda tem LOGICUP_TOKEN (Path=/api) e XSRF-TOKEN (Path=/) no navegador.
 * Eles já não valem nada (o backend só lê os nomes novos), mas ficariam lá até expirar.
 * Para o navegador apagar um cookie, a remoção precisa ter o MESMO nome, Path e Domain da criação,
 * por isso cada um é apagado com o Path com que foi criado.
 *
 * Pode ser retirado algumas sprints depois do deploy (quando nenhum navegador tiver mais os nomes antigos).
 */
public final class CookiesLegados {

    private CookiesLegados() {}

    /** Valores prontos para o cabeçalho Set-Cookie, com Max-Age=0 (apagar). */
    public static String[] paraApagar() {
        return List.of(
                ResponseCookie.from("LOGICUP_TOKEN", "").path("/api").maxAge(0)
                        .httpOnly(true).secure(true).sameSite("Strict").build(),
                ResponseCookie.from("XSRF-TOKEN", "").path("/").maxAge(0)
                        .secure(true).sameSite("Strict").build()
        ).stream().map(ResponseCookie::toString).toArray(String[]::new);
    }
}
