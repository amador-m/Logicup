package br.com.logicup.backend.controller;

import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.QuestaoNivelamento;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.QuestaoNivelamentoRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 11: QUIZ DE NIVELAMENTO de ponta a ponta, pela API, com as 10 questões reais da migração 13.
 * Confere também o efeito na TRILHA: as fases dominadas ficam liberadas, mas sem XP e sem virar "aprovadas".
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NivelamentoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private QuestaoNivelamentoRepository questaoRepository;
    @Autowired private JdbcTemplate jdbc;

    private Aluno aluno;
    private List<QuestaoNivelamento> questoes;

    @BeforeEach
    void preparar() {
        aluno = alunoRepository.save(ContasDeTeste.confirmada(
                new Aluno("Aluna Nivelamento", "it.nivel", "it.nivel@teste.com", "hash")));
        questoes = questaoRepository.findAllByOrderByFaseAscOrdenacaoAsc();
    }

    private static RequestPostProcessor como(Usuario usuario) {
        String papel = usuario.getTipoUsuario().name();
        return jwt().jwt(t -> t.subject(String.valueOf(usuario.getId())).claim("role", papel)
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_" + papel));
    }

    /** Monta o JSON das respostas: acerta tudo, MENOS as questões das fases/ordens indicadas em "errar". */
    private String respostas(Set<String> errar) {
        return questoes.stream()
                .map(q -> {
                    boolean erra = errar.contains(q.getFase() + "." + q.getOrdenacao());
                    String alternativa = erra ? ("a".equals(q.getCorreta()) ? "b" : "a") : q.getCorreta();
                    return "{\"questaoId\": %d, \"alternativa\": \"%s\"}".formatted(q.getId(), alternativa);
                })
                .collect(Collectors.joining(", ", "{\"respostas\": [", "]}"));
    }

    private ResultActions responder(Usuario quem, String corpo) throws Exception {
        return mockMvc.perform(post("/api/nivelamento/respostas").with(como(quem)).with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions trilha() throws Exception {
        return mockMvc.perform(get("/api/trilha").with(como(aluno)));
    }

    @Test
    void alunoNovoVeAsQuestoesSemAResposta() throws Exception {
        mockMvc.perform(get("/api/nivelamento").with(como(aluno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("PENDENTE"))
                .andExpect(jsonPath("$.questoes", hasSize(10)))
                .andExpect(jsonPath("$.questoes[0].alternativas", hasSize(4)))
                .andExpect(jsonPath("$.questoes[0].correta").doesNotExist())
                .andExpect(jsonPath("$.questoes[0].explicacao").doesNotExist());
    }

    @Test
    void dominaAsFases1a3ELiberaAteAFase4SemGanharXp() throws Exception {
        // erra uma questão da Fase 4: as Fases 1, 2 e 3 contam; a 5 não conta, mesmo acertando
        responder(aluno, respostas(Set.of("4.1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acertos").value(9))
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.fasesLiberadas").value(3))
                .andExpect(jsonPath("$.nivelAtual").value(4))
                .andExpect(jsonPath("$.correcoes", hasSize(10)))
                .andExpect(jsonPath("$.correcoes[6].acertou").value(false))
                .andExpect(jsonPath("$.correcoes[6].explicacao").isNotEmpty());

        trilha().andExpect(jsonPath("$.aluno.xp").value(0))
                .andExpect(jsonPath("$.aluno.nivel").value(4))
                // fases 1 a 3 liberadas, mas NÃO aprovadas (o aluno não fez as rodadas)
                .andExpect(jsonPath("$.fases[0].rodadas[0].status").value("LIBERADA"))
                .andExpect(jsonPath("$.fases[2].rodadas[1].status").value("LIBERADA"))
                // a 1ª rodada da Fase 4 libera; o resto continua em sequência
                .andExpect(jsonPath("$.fases[3].rodadas[0].status").value("LIBERADA"))
                .andExpect(jsonPath("$.fases[3].rodadas[1].status").value("BLOQUEADA"))
                .andExpect(jsonPath("$.fases[4].rodadas[0].status").value("BLOQUEADA"));

        mockMvc.perform(get("/api/nivelamento").with(como(aluno)))
                .andExpect(jsonPath("$.situacao").value("FEITO"))
                .andExpect(jsonPath("$.fasesLiberadas").value(3))
                .andExpect(jsonPath("$.questoes").doesNotExist());
    }

    @Test
    void soPodeFazerUmaVez() throws Exception {
        responder(aluno, respostas(Set.of())).andExpect(status().isOk())
                .andExpect(jsonPath("$.fasesLiberadas").value(5));
        responder(aluno, respostas(Set.of())).andExpect(status().isConflict());
        mockMvc.perform(post("/api/nivelamento/recusa").with(como(aluno)).with(csrfValido()))
                .andExpect(status().isConflict());
    }

    @Test
    void errarAFase1NaoLiberaNada() throws Exception {
        responder(aluno, respostas(Set.of("1.2"))).andExpect(jsonPath("$.fasesLiberadas").value(0))
                .andExpect(jsonPath("$.nivelAtual").value(1));
        trilha().andExpect(jsonPath("$.fases[0].rodadas[0].status").value("LIBERADA"))
                .andExpect(jsonPath("$.fases[0].rodadas[1].status").value("BLOQUEADA"));
    }

    @Test
    void comecarDoZeroGravaAEscolhaENaoPerguntaDeNovo() throws Exception {
        mockMvc.perform(post("/api/nivelamento/recusa").with(como(aluno)).with(csrfValido()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/nivelamento").with(como(aluno)))
                .andExpect(jsonPath("$.situacao").value("NAO_QUIS"));
        trilha().andExpect(jsonPath("$.fases[0].rodadas[1].status").value("BLOQUEADA"));
        responder(aluno, respostas(Set.of())).andExpect(status().isConflict());
    }

    @Test
    void quemJaComecouATrilhaNaoFazMaisONivelamento() throws Exception {
        Integer exercicio = jdbc.queryForObject("SELECT MIN(id_exercicio) FROM exercicio", Integer.class);
        jdbc.update("INSERT INTO progresso_exercicio (id_usuario, id_exercicio, status, tentativas, pontos_ganhos) "
                + "VALUES (?, ?, 'EM_ANDAMENTO', 1, 0)", aluno.getId(), exercicio);

        mockMvc.perform(get("/api/nivelamento").with(como(aluno)))
                .andExpect(jsonPath("$.situacao").value("NAO_DISPONIVEL"))
                .andExpect(jsonPath("$.questoes").doesNotExist());
        responder(aluno, respostas(Set.of())).andExpect(status().isConflict());
    }

    @Test
    void precisaResponderTodasAsQuestoesUmaVezCada() throws Exception {
        QuestaoNivelamento primeira = questoes.get(0);
        responder(aluno, "{\"respostas\": [{\"questaoId\": %d, \"alternativa\": \"a\"}]}".formatted(primeira.getId()))
                .andExpect(status().isBadRequest());
        String repetida = respostas(Set.of()).replace("]}",
                ", {\"questaoId\": %d, \"alternativa\": \"a\"}]}".formatted(primeira.getId()));
        responder(aluno, repetida).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/nivelamento").with(como(aluno)))
                .andExpect(jsonPath("$.situacao").value("PENDENTE")); // nada foi gravado
    }

    @Test
    void professorNaoTemNivelamento() throws Exception {
        Usuario professor = usuarioRepository.save(ContasDeTeste.confirmada(new Usuario("Prof", "it.nivel.prof",
                "it.nivel.prof@teste.com", "hash", TipoUsuario.PROFESSOR)));
        mockMvc.perform(get("/api/nivelamento").with(como(professor))).andExpect(status().isForbidden());
    }

    @Test
    void semLoginNaoEntra() throws Exception {
        mockMvc.perform(get("/api/nivelamento")).andExpect(status().isUnauthorized());
    }
}
