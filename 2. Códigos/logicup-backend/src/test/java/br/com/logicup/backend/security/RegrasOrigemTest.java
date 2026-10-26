package br.com.logicup.backend.security;

import br.com.logicup.backend.security.RegrasOrigem.Decisao;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Teste UNITÁRIO da regra de origem (sem Spring, sem HTTP).
 * Os testes de integração (SegurancaRequisicoesIntegrationTest) provam a regra dentro da cadeia de filtros;
 * aqui fica a tabela completa de casos, rápida de rodar e fácil de ler.
 */
class RegrasOrigemTest {

    private static final String CONFIAVEL = "http://localhost:5173";
    private final RegrasOrigem regras = new RegrasOrigem(List.of(CONFIAVEL));

    // ---------- lista de origens confiáveis (validada na subida) ----------

    @Test
    void listaENormalizada() {
        RegrasOrigem r = new RegrasOrigem(List.of(" HTTP://LocalHost:5173 ", "https://logicup.exemplo.com:443"));
        assertEquals(Set.of("http://localhost:5173", "https://logicup.exemplo.com"), r.origensConfiaveis());
    }

    @Test
    void listaVaziaImpedeASubida() {
        assertThrows(IllegalArgumentException.class, () -> new RegrasOrigem(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new RegrasOrigem(null));
    }

    @Test
    void itensInvalidosNaListaImpedemASubida() {
        for (String invalida : List.of("*", "https://*.exemplo.com", "null", "  ", "localhost:5173",
                "ftp://exemplo.com", "http://localhost:5173/", "http://localhost:5173/api",
                "http://localhost:5173?x=1", "http://usuario:senha@exemplo.com")) {
            assertThrows(IllegalArgumentException.class, () -> new RegrasOrigem(List.of(invalida)),
                    "deveria recusar: " + invalida);
        }
    }

    // ---------- sem cabeçalhos ----------

    @Test
    void semNenhumCabecalhoDecideOCsrf() {
        assertEquals(Decisao.SEM_CABECALHOS, regras.avaliar(null, null, null));
    }

    // ---------- um cabeçalho só ----------

    @Test
    void secFetchSite() {
        assertEquals(Decisao.PERMITIDA, regras.avaliar("same-origin", null, null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar("same-site", null, null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar("cross-site", null, null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar("none", null, null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar("valor-estranho", null, null));
    }

    @Test
    void origin() {
        assertEquals(Decisao.PERMITIDA, regras.avaliar(null, "http://localhost:5173", null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, "https://site-malicioso.com", null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, "null", null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, "", null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, "http://localhost:5173.evil.com", null)); // sufixo
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, "http://evil.com/http://localhost:5173", null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, "http://localhost:8000", null));  // outra porta
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, "https://localhost:5173", null)); // outro esquema
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, "http://127.0.0.1:5173", null));  // outro host
    }

    @Test
    void referer() {
        assertEquals(Decisao.PERMITIDA, regras.avaliar(null, null, "http://localhost:5173/perfil?aba=senha"));
        assertEquals(Decisao.PERMITIDA, regras.avaliar(null, null, "http://localhost:5173"));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, null, "https://site-malicioso.com/ataque"));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, null, "http://localhost:5173.evil.com/x"));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, null, "isto não é uma url"));
    }

    // ---------- combinações: todos os presentes precisam ser compatíveis ----------

    @Test
    void todosCompativeisPassam() {
        assertEquals(Decisao.PERMITIDA,
                regras.avaliar("same-origin", "http://localhost:5173", "http://localhost:5173/perfil"));
    }

    @Test
    void umBomNaoCompensaUmRuim() {
        assertEquals(Decisao.BLOQUEADA, regras.avaliar("same-origin", "https://site-malicioso.com", null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar("cross-site", "http://localhost:5173", null));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar(null, "http://localhost:5173", "https://site-malicioso.com/x"));
        assertEquals(Decisao.BLOQUEADA, regras.avaliar("same-origin", "http://localhost:5173", "https://site-malicioso.com/x"));
    }
}
