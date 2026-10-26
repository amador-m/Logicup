package br.com.logicup.backend.controller;

import br.com.logicup.backend.security.TokenService;
import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.Modulo;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.TipoExercicio;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ModuloRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de INTEGRAÇÃO da ementa (Sprint 7): GET /api/ementa.
 *
 * Mesma estratégia do TrilhaRodadaIntegrationTest: uma fase própria de teste com ordenação NEGATIVA
 * (-300), que vem ANTES da Fase 1 real -> fases[0] é sempre a fase de teste, e o teste não depende
 * do conteúdo do seed. @Transactional desfaz tudo no fim de cada teste.
 *
 * Precisa da coluna exercicio.habilidade no banco (migracao_s7_habilidade.sql, PASSO 1).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmentaIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;

    private static final String CONTEUDO_COM_OBJETIVOS = """
            {"objetivos":["Objetivo 1 de teste","Objetivo 2 de teste"],"teoria":[],"exemplos":[]}""";
    private static final String CONTEUDO_SEM_OBJETIVOS = """
            {"teoria":[{"titulo":"T","texto":"Texto"}],"exemplos":[]}""";

    @BeforeEach
    void montarFaseDeTeste() {
        Modulo fase = moduloRepository.save(new Modulo("Fase de teste da ementa", "Objetivo geral de teste", -300));
        Rodada rodada1 = rodadaRepository.save(new Rodada(fase, "Rodada E1", 1, CONTEUDO_COM_OBJETIVOS));
        Rodada rodada2 = rodadaRepository.save(new Rodada(fase, "Rodada E2", 2, CONTEUDO_SEM_OBJETIVOS));

        // salvas FORA de ordem de propósito: a ementa precisa ordenar pela posição (ordenacao)
        exercicio(rodada1, "IT.E1.Q2", 2, null);
        exercicio(rodada1, "IT.E1.Q1", 1, "Habilidade de teste da Q1");
        exercicio(rodada2, "IT.E2.Q1", 1, null);
    }

    private void exercicio(Rodada rodada, String codigo, int ordem, String habilidade) {
        Exercicio exercicio = new Exercicio(rodada, codigo, "Questão " + codigo, ordem,
                TipoExercicio.MULTIPLA_ESCOLHA, FormatoResposta.ESCOLHA_UNICA, 10,
                "Enunciado secreto de teste", "{\"correta\":\"a\"}", "Explicação de teste");
        exercicio.setAlternativasJson("[{\"id\":\"a\",\"texto\":\"Alternativa de teste\"}]");
        exercicio.setHabilidade(habilidade);
        exercicioRepository.save(exercicio);
    }

    private static RequestPostProcessor logadoComo(String role) {
        return jwt().jwt(token -> token.subject("999999").claim("role", role)
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR)) // Sprint 8: sessão com o aceite vigente dos Termos
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void semLoginRecebe401() throws Exception {
        mockMvc.perform(get("/api/ementa")).andExpect(status().isUnauthorized());
    }

    @Test
    void alunoRecebe403() throws Exception {
        mockMvc.perform(get("/api/ementa").with(logadoComo("ALUNO"))).andExpect(status().isForbidden());
    }

    @Test
    void professorVeFaseRodadasEQuestoesEmOrdem() throws Exception {
        mockMvc.perform(get("/api/ementa").with(logadoComo("PROFESSOR")))
                .andExpect(status().isOk())
                // fase: objetivo geral = modulo.descricao
                .andExpect(jsonPath("$.fases[0].ordem").value(-300))
                .andExpect(jsonPath("$.fases[0].objetivoGeral").value("Objetivo geral de teste"))
                // rodada 1: objetivos vindos do conteudo_teorico
                .andExpect(jsonPath("$.fases[0].rodadas[0].titulo").value("Rodada E1"))
                .andExpect(jsonPath("$.fases[0].rodadas[0].objetivos.length()").value(2))
                .andExpect(jsonPath("$.fases[0].rodadas[0].objetivos[0]").value("Objetivo 1 de teste"))
                // questões em ordem de posição, mesmo salvas fora de ordem
                .andExpect(jsonPath("$.fases[0].rodadas[0].questoes[0].codigo").value("IT.E1.Q1"))
                .andExpect(jsonPath("$.fases[0].rodadas[0].questoes[0].habilidade").value("Habilidade de teste da Q1"))
                .andExpect(jsonPath("$.fases[0].rodadas[0].questoes[0].tipo").value("MULTIPLA_ESCOLHA"))
                .andExpect(jsonPath("$.fases[0].rodadas[0].questoes[1].codigo").value("IT.E1.Q2"))
                // habilidade ainda não escrita -> null (o frontend mostra "a definir")
                .andExpect(jsonPath("$.fases[0].rodadas[0].questoes[1].habilidade").value(nullValue()))
                // rodada sem "objetivos" no JSON -> lista vazia, e não erro 500
                .andExpect(jsonPath("$.fases[0].rodadas[1].objetivos.length()").value(0))
                .andExpect(jsonPath("$.fases[0].rodadas[1].questoes.length()").value(1));
    }

    @Test
    void adminTambemVe() throws Exception {
        mockMvc.perform(get("/api/ementa").with(logadoComo("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fases[0].ordem").value(-300));
    }

    @Test
    void ementaNaoExpoeGabaritoNemEnunciado() throws Exception {
        mockMvc.perform(get("/api/ementa").with(logadoComo("PROFESSOR")))
                .andExpect(status().isOk())
                // nenhum CAMPO desses existe no DTO...
                .andExpect(content().string(not(containsString("gabarito"))))
                .andExpect(content().string(not(containsString("\"enunciado\""))))
                .andExpect(content().string(not(containsString("\"alternativas\""))))
                .andExpect(content().string(not(containsString("\"explicacao\""))))
                // ...e nenhum VALOR da questão de teste vaza por outro caminho.
                // (não dá para procurar a palavra "correta": a habilidade real da F1.Q6 tem "posição correta")
                .andExpect(content().string(not(containsString("Enunciado secreto de teste"))))
                .andExpect(content().string(not(containsString("Alternativa de teste"))))
                .andExpect(content().string(not(containsString("Explicação de teste"))));
    }
}
