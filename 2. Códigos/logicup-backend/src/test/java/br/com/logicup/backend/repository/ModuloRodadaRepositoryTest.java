package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Modulo;
import br.com.logicup.backend.model.Rodada;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @DataJpaTest: sobe só a parte de JPA (entidades + repositories), sem Tomcat nem controllers.
 * Cada teste roda dentro de uma transação que é DESFEITA (rollback) no final,
 * então nada do que for inserido aqui fica gravado no banco.
 *
 * @AutoConfigureTestDatabase(replace = NONE): usa o MySQL real configurado no
 * application.properties (por padrão o Spring tentaria um banco em memória, que não temos).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ModuloRodadaRepositoryTest {

    private static final String CONTEUDO_VAZIO = "{\"teoria\": [], \"exemplos\": []}";

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private RodadaRepository rodadaRepository;

    @Test
    void deveBuscarRodadasDoModuloEmOrdem() {
        // ordenação alta para não colidir com fases reais (ordenacao é UNIQUE)
        Modulo modulo = moduloRepository.save(new Modulo("Módulo de teste", null, 999));

        // salvas fora de ordem de propósito
        rodadaRepository.save(new Rodada(modulo, "Rodada B", 2, CONTEUDO_VAZIO));
        rodadaRepository.save(new Rodada(modulo, "Rodada A", 1, CONTEUDO_VAZIO));

        List<Rodada> rodadas = rodadaRepository.findByModuloIdOrderByOrdenacaoAsc(modulo.getId());

        assertEquals(2, rodadas.size());
        assertEquals("Rodada A", rodadas.get(0).getTitulo()); // veio ordenada
        assertEquals("Rodada B", rodadas.get(1).getTitulo());
    }

    @Test
    void deveBuscarModuloPelaOrdenacao() {
        moduloRepository.save(new Modulo("Módulo de teste", "descrição", 998));

        Modulo encontrado = moduloRepository.findByOrdenacao(998).orElseThrow();

        assertEquals("Módulo de teste", encontrado.getTitulo());
    }
}