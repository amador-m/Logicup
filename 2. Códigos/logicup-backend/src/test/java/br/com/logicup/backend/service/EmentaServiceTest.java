package br.com.logicup.backend.service;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste UNITÁRIO (sem Spring e sem banco) da leitura dos objetivos da rodada.
 * Garante que conteúdo incompleto ou com formato errado vira lista vazia, e não erro 500.
 */
class EmentaServiceTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private List<String> ler(String json) {
        return EmentaService.lerObjetivos(jsonMapper, json, 1);
    }

    @Test
    void leOsObjetivosEIgnoraTeoriaEExemplos() {
        String json = """
                {"objetivos":["Compreender X","Aplicar Y"],"teoria":[{"titulo":"T","texto":"..."}],"exemplos":[]}""";
        assertEquals(List.of("Compreender X", "Aplicar Y"), ler(json));
    }

    @Test
    void semCampoObjetivosDevolveListaVazia() {
        assertEquals(List.of(), ler("{\"teoria\":[]}"));
    }

    @Test
    void objetivosNullDevolveListaVazia() {
        assertEquals(List.of(), ler("{\"objetivos\":null}"));
    }

    @Test
    void conteudoNuloOuEmBrancoDevolveListaVazia() {
        assertEquals(List.of(), ler(null));
        assertEquals(List.of(), ler("  "));
    }

    @Test
    void jsonQuebradoDevolveListaVazia() {
        assertEquals(List.of(), ler("{\"objetivos\":[\"sem fechar\""));
    }

    @Test
    void objetivosNoFormatoErradoDevolveListaVazia() {
        assertEquals(List.of(), ler("{\"objetivos\":{\"a\":1}}"));
    }

    @Test
    void itensNulosSaoIgnorados() {
        assertEquals(List.of("Válido"), ler("{\"objetivos\":[null,\"Válido\"]}"));
    }
}
