package br.com.logicup.backend.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Sprint 9 (R3): teste de UNIDADE do gerador de código de convite (sem Spring, sem banco). */
class GeradorCodigoConviteTest {

    private final GeradorCodigoConvite gerador = new GeradorCodigoConvite();

    @Test
    void alfabetoTem31CaracteresSemOsQueSeConfundem() {
        assertEquals(31, GeradorCodigoConvite.ALFABETO.length());
        for (char ambiguo : "01ILO".toCharArray()) {
            assertFalse(GeradorCodigoConvite.ALFABETO.indexOf(ambiguo) >= 0, "não pode ter " + ambiguo);
        }
    }

    @Test
    void codigoTem8CaracteresDoAlfabeto() {
        for (int i = 0; i < 1_000; i++) {
            String codigo = gerador.gerar();
            assertTrue(codigo.matches("^[ABCDEFGHJKMNPQRSTUVWXYZ23456789]{8}$"), codigo);
        }
    }

    @Test
    void codigosNaoSeRepetemNumaAmostraGrande() {
        Set<String> vistos = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            vistos.add(gerador.gerar());
        }
        // com ~852 bilhões de combinações, uma repetição em 10 mil sorteios é praticamente impossível
        assertEquals(10_000, vistos.size());
    }
}
