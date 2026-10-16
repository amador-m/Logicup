package br.com.logicup.backend.controller;

import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de INTEGRAÇÃO do GET /api/ranking (banco real, tudo desfeito no fim por causa do @Transactional).
 *
 * O banco de desenvolvimento já tem alunos reais com XP. Para o teste não depender deles,
 * os alunos criados aqui têm XP acima de 1.000.000: assim ocupam o topo com certeza.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RankingIntegrationTest {

    private static final int BASE = 1_000_000;

    @Autowired private MockMvc mockMvc;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Aluno aluno(String username, int xp) {
        Aluno aluno = new Aluno("Aluno " + username, username, username + "@teste.com", "hash");
        aluno.adicionarPontos(xp); // única forma de mudar o XP (regra encapsulada na entidade)
        return alunoRepository.save(aluno);
    }

    private static RequestPostProcessor logadoComo(Integer id, String role) {
        return jwt().jwt(token -> token.subject(String.valueOf(id)).claim("role", role))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void topoOrdenadoPorXpComEmpateNaMesmaPosicao() throws Exception {
        aluno("it.rk.a", BASE + 300);
        aluno("it.rk.c", BASE + 200);  // empata com o "b": ordem alfabética -> b antes de c
        aluno("it.rk.b", BASE + 200);
        Aluno eu = aluno("it.rk.d", BASE + 100);

        mockMvc.perform(get("/api/ranking").with(logadoComo(eu.getId(), "ALUNO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topo[0].username").value("it.rk.a"))
                .andExpect(jsonPath("$.topo[0].posicao").value(1))
                .andExpect(jsonPath("$.topo[1].username").value("it.rk.b"))
                .andExpect(jsonPath("$.topo[1].posicao").value(2))
                .andExpect(jsonPath("$.topo[2].username").value("it.rk.c"))
                .andExpect(jsonPath("$.topo[2].posicao").value(2))   // empate: mesma posição
                .andExpect(jsonPath("$.topo[3].username").value("it.rk.d"))
                .andExpect(jsonPath("$.topo[3].posicao").value(4))   // e a seguinte pula o 3º
                .andExpect(jsonPath("$.topo[3].voce").value(true))
                .andExpect(jsonPath("$.voce.posicao").value(4))
                // privacidade: nome e e-mail nunca saem no ranking
                .andExpect(jsonPath("$.topo[0].nome").doesNotExist())
                .andExpect(jsonPath("$.topo[0].email").doesNotExist());
    }

    @Test
    void foraDoTop10OAlunoVeSuaPosicao() throws Exception {
        for (int i = 1; i <= 11; i++) {
            aluno("it.rk.top" + i, BASE + 100 + i);
        }
        Aluno eu = aluno("it.rk.eu", BASE + 1); // atrás dos 11 acima

        mockMvc.perform(get("/api/ranking").with(logadoComo(eu.getId(), "ALUNO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topo.length()").value(10))
                .andExpect(jsonPath("$.topo[*].username", not(hasItem("it.rk.eu"))))
                .andExpect(jsonPath("$.voce.username").value("it.rk.eu"))
                .andExpect(jsonPath("$.voce.posicao").value(12));
    }

    @Test
    void alunoSemXpNaoEntraNoTopoMasVeQueAindaNaoPontuou() throws Exception {
        Aluno novato = aluno("it.rk.novato", 0);

        mockMvc.perform(get("/api/ranking").with(logadoComo(novato.getId(), "ALUNO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topo[*].username", not(hasItem("it.rk.novato"))))
                .andExpect(jsonPath("$.voce.username").value("it.rk.novato"))
                .andExpect(jsonPath("$.voce.posicao").value(nullValue()));
    }

    @Test
    void professorNaoApareceENaoTemPosicao() throws Exception {
        aluno("it.rk.aluno", BASE + 50);
        Usuario professor = usuarioRepository.save(
                new Usuario("Professor", "it.rk.prof", "it.rk.prof@teste.com", "hash", TipoUsuario.PROFESSOR));

        mockMvc.perform(get("/api/ranking").with(logadoComo(professor.getId(), "PROFESSOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topo[0].username").value("it.rk.aluno"))
                .andExpect(jsonPath("$.topo[*].username", not(hasItem("it.rk.prof"))))
                .andExpect(jsonPath("$.voce").value(nullValue()));
    }

    @Test
    void rankingExigeLogin() throws Exception {
        mockMvc.perform(get("/api/ranking"))
                .andExpect(status().isUnauthorized());
    }
}
