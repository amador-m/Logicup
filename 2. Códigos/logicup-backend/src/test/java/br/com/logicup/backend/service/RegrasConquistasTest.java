package br.com.logicup.backend.service;

import br.com.logicup.backend.model.Avatar;
import br.com.logicup.backend.model.Conquista;
import br.com.logicup.backend.model.NivelConquista;
import br.com.logicup.backend.service.RegrasConquistas.Alcancada;
import br.com.logicup.backend.service.RegrasConquistas.MetricasAluno;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sprint 9 — testes de UNIDADE das regras puras das conquistas e dos catálogos (sem Spring, sem banco).
 * Rodam em milissegundos: são a primeira rede de proteção quando alguém mexer em metas ou preços.
 */
class RegrasConquistasTest {

    private static MetricasAluno metricas(int questoes, int rodadas, int xp, int ofensiva, int dePrimeira,
                                          int bosses, int fases, boolean turma) {
        return new MetricasAluno(questoes, rodadas, xp, ofensiva, dePrimeira, bosses, fases, turma);
    }

    private static List<String> nomes(List<Alcancada> alcancadas) {
        return alcancadas.stream().map(a -> a.conquista().name() + ":" + a.meta().nivel()).toList();
    }

    @Test
    void alunoSemNadaNaoAlcancaNenhuma() {
        assertTrue(RegrasConquistas.alcancadas(metricas(0, 0, 0, 0, 0, 0, 0, false)).isEmpty());
    }

    @Test
    void metaExataJaAlcancaONivel() {
        // >= : exatamente 100 XP já é o bronze de XP; 99 não é
        assertEquals(List.of("XP:BRONZE"), nomes(RegrasConquistas.alcancadas(metricas(0, 0, 100, 0, 0, 0, 0, false))));
        assertTrue(RegrasConquistas.alcancadas(metricas(0, 0, 99, 0, 0, 0, 0, false)).isEmpty());
    }

    @Test
    void niveisAcumulam() {
        // 7 dias de ofensiva = bronze (3) E prata (7), mas não ouro (30)
        assertEquals(List.of("OFENSIVA:BRONZE", "OFENSIVA:PRATA"),
                nomes(RegrasConquistas.alcancadas(metricas(0, 0, 0, 7, 0, 0, 0, false))));
    }

    @Test
    void conquistasDeNivelUnico() {
        assertEquals(List.of("PRIMEIRO_ACERTO:UNICA", "TURMA:UNICA"),
                nomes(RegrasConquistas.alcancadas(metricas(1, 0, 0, 0, 0, 0, 0, true))));
    }

    @Test
    void tudoNoMaximoAlcancaTodosOsNiveis() {
        List<Alcancada> todas = RegrasConquistas.alcancadas(metricas(999, 999, 99999, 999, 999, 999, 999, true));
        int esperado = Arrays.stream(Conquista.values()).mapToInt(c -> c.getMetas().size()).sum();
        assertEquals(esperado, todas.size());
        assertEquals(20, esperado); // 6 conquistas de 3 níveis + 2 de nível único (decisão C2)
    }

    @Test
    void gemasPorNivelSaoAsDaDecisaoM4() {
        assertEquals(15, NivelConquista.UNICA.getGemas());
        assertEquals(15, NivelConquista.BRONZE.getGemas());
        assertEquals(30, NivelConquista.PRATA.getGemas());
        assertEquals(60, NivelConquista.OURO.getGemas());
    }

    @Test
    void descricaoTrocaAMetaDeCadaNivel() {
        Conquista.Meta prata = Conquista.XP.getMetas().get(1);
        assertEquals("Junte 500 XP.", Conquista.XP.descricaoDe(prata));
        assertEquals("Entre em uma turma com o código do professor.",
                Conquista.TURMA.descricaoDe(Conquista.TURMA.getMetas().get(0)));
    }

    @Test
    void codigoDesconhecidoNaoQuebra() {
        assertTrue(Conquista.porCodigo("NAO_EXISTE").isEmpty());
        assertTrue(Avatar.porCodigo("NAO_EXISTE").isEmpty());
        assertTrue(Avatar.porCodigo(null).isEmpty());
    }

    @Test
    void doisAvataresGratuitosEOsPagosNaoCabemNasTresPrimeirasFases() {
        assertEquals(List.of(Avatar.GATO_CLARO, Avatar.GATO_ESCURO),
                Arrays.stream(Avatar.values()).filter(Avatar::isGratuito).toList());
        assertEquals(Avatar.GATO_CLARO, Avatar.PADRAO);
        assertFalse(Avatar.CACHORRO_CLARO.isGratuito());
        // pedido da Maria (M4): não deve dar para comprar TODOS até a Fase 3 (estimativa máxima ≈ 360 gemas)
        int totalDosPagos = Arrays.stream(Avatar.values()).mapToInt(Avatar::getPreco).sum();
        assertEquals(760, totalDosPagos);
        assertTrue(totalDosPagos > 360);
    }
}
