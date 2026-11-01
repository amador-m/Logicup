package br.com.logicup.backend.service.correcao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // Sprint 9 (C3)
    @Test
    void termoPrecisaComecarUmaPalavra() {
        assertTrue(NormalizadorTexto.contemNoInicioDePalavra("primeiro ligar o kart", "lig"));   // radical
        assertTrue(NormalizadorTexto.contemNoInicioDePalavra("lig", "lig"));                     // texto inteiro
        assertFalse(NormalizadorTexto.contemNoInicioDePalavra("religar o kart", "lig"));         // meio da palavra
        assertTrue(NormalizadorTexto.contemNoInicioDePalavra("religar e depois ligar", "lig"));  // 2ª ocorrência vale
        assertTrue(NormalizadorTexto.contemNoInicioDePalavra("x=valor;", "valor"));              // depois de símbolo
        assertFalse(NormalizadorTexto.contemNoInicioDePalavra("valor2", "2"));                   // dígito colado
    }

    @Test
    void termoQueComecaComSimboloValeEmQualquerPosicao() {
        assertTrue(NormalizadorTexto.contemNoInicioDePalavra("energia==0", "==0"));
        assertTrue(NormalizadorTexto.contemNoInicioDePalavra("i++", "++"));
        assertFalse(NormalizadorTexto.contemNoInicioDePalavra("algo", ""));
        assertFalse(NormalizadorTexto.contemNoInicioDePalavra(null, "lig"));
    }
}
