package br.com.logicup.backend.service.correcao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NormalizadorTextoTest {

    @Test
    void palavrasChaveIgnoramMaiusculasAcentosEEspacos() {
        assertEquals("veiculo passando", NormalizadorTexto.paraPalavrasChave("  Veículo    PASSANDO "));
        assertEquals("transito", NormalizadorTexto.paraPalavrasChave("Trânsito"));
        assertEquals("", NormalizadorTexto.paraPalavrasChave(null));
    }

    @Test
    void respostaCurtaTiraPontoFinalEEspacos() {
        assertEquals("excluir", NormalizadorTexto.paraRespostaCurta(" Excluir. ", false));
        assertEquals("Au au!", NormalizadorTexto.paraRespostaCurta("Au  au!", true));
    }

    @Test
    void respostaCurtaRespeitaAFlagDeMaiusculas() {
        assertEquals("System.out.println", NormalizadorTexto.paraRespostaCurta("System.out.println", true));
        assertEquals("system.out.println", NormalizadorTexto.paraRespostaCurta("System.out.println", false));
    }
}
