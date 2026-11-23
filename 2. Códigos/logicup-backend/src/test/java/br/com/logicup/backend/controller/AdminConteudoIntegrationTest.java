package br.com.logicup.backend.controller;

import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 11: GESTÃO DE CONTEÚDOS pelo admin, com o conteúdo REAL da Fase 1 (Rodada 1).
 * Edita um parágrafo da teoria e o enunciado de uma questão, e confere o que fica gravado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminConteudoIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private JsonMapper jsonMapper;
    @Autowired private EntityManager entityManager;

    private Usuario admin;
    private Rodada rodada;

    @BeforeEach
    void preparar() {
        admin = usuarioRepository.save(ContasDeTeste.confirmada(new Usuario("Admin Conteúdo", "it.cont.admin",
                "it.cont.admin@teste.com", "hash", TipoUsuario.ADMIN)));
        rodada = rodadaRepository.findAllEmOrdemDaTrilha().stream()
                .filter(r -> r.getModulo().getOrdenacao() == 1 && r.getOrdenacao() == 1)
                .findFirst().orElseThrow();
    }

    private static RequestPostProcessor como(Usuario usuario) {
        String papel = usuario.getTipoUsuario().name();
        return jwt().jwt(t -> t.subject(String.valueOf(usuario.getId())).claim("role", papel)
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_" + papel));
    }

    private ResultActions salvarRodada(String titulo, Object conteudo) throws Exception {
        String corpo = jsonMapper.writeValueAsString(java.util.Map.of("titulo", titulo, "conteudo", conteudo));
        return mockMvc.perform(put("/api/admin/rodadas/{id}", rodada.getId()).with(como(admin)).with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ObjectNode conteudoAtual() {
        return (ObjectNode) jsonMapper.readTree(rodada.getConteudoTeorico());
    }

    @Test
    void arvoreTemAs10FasesComRodadasEExercicios() throws Exception {
        mockMvc.perform(get("/api/admin/conteudo").with(como(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fases", hasSize(10)))
                .andExpect(jsonPath("$.fases[0].rodadas", hasSize(2)))
                .andExpect(jsonPath("$.fases[0].rodadas[0].exercicios[0].codigo").isNotEmpty());
    }

    @Test
    void rodadaVemComTeoriaETextosMasSemGabarito() throws Exception {
        mockMvc.perform(get("/api/admin/rodadas/{id}", rodada.getId()).with(como(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conteudo.teoria[0].blocos[0].tipo").value("paragrafo"))
                .andExpect(jsonPath("$.exercicios[0].enunciado").isNotEmpty())
                .andExpect(jsonPath("$.exercicios[0].gabarito").doesNotExist())
                .andExpect(jsonPath("$.exercicios[0].alternativas").doesNotExist());
    }

    @Test
    void editarUmParagrafoGravaERecalculaOTextoDaSecao() throws Exception {
        ObjectNode conteudo = conteudoAtual();
        ((ObjectNode) conteudo.get("teoria").get(0).get("blocos").get(0)).put("texto", "Texto revisado pela equipe.");

        salvarRodada("Algoritmos (revisado)", conteudo)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Algoritmos (revisado)"))
                .andExpect(jsonPath("$.conteudo.teoria[0].blocos[0].texto").value("Texto revisado pela equipe."));
        entityManager.flush();
        entityManager.clear();

        Rodada gravada = rodadaRepository.findById(rodada.getId()).orElseThrow();
        assertEquals("Algoritmos (revisado)", gravada.getTitulo());
        String textoDaSecao = jsonMapper.readTree(gravada.getConteudoTeorico()).get("teoria").get(0).get("texto").asString();
        assertTrue(textoDaSecao.startsWith("Texto revisado pela equipe."), textoDaSecao);
    }

    @Test
    void naoDaParaMudarAEstrutura() throws Exception {
        ObjectNode semBloco = conteudoAtual();
        ((ArrayNode) semBloco.get("teoria").get(0).get("blocos")).remove(0);
        salvarRodada("Título", semBloco).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("conteudo.teoria[0].blocos")));

        ObjectNode vazio = conteudoAtual();
        ((ObjectNode) vazio.get("teoria").get(0).get("blocos").get(0)).put("texto", " ");
        salvarRodada("Título", vazio).andExpect(status().isBadRequest());

        salvarRodada("  ", conteudoAtual()).andExpect(status().isBadRequest());
    }

    @Test
    void editarOsTextosDeUmExercicio() throws Exception {
        Exercicio exercicio = exercicioRepository.findByRodadaIdOrderByOrdenacaoAsc(rodada.getId()).get(0);
        String gabaritoAntes = exercicio.getGabaritoJson();

        mockMvc.perform(put("/api/admin/exercicios/{id}", exercicio.getId()).with(como(admin)).with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"Novo título\", \"enunciado\": \"Novo enunciado?\", "
                                + "\"explicacao\": \"Nova explicação.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enunciado").value("Novo enunciado?"));
        entityManager.flush();
        entityManager.clear();

        Exercicio gravado = exercicioRepository.findById(exercicio.getId()).orElseThrow();
        assertEquals("Novo enunciado?", gravado.getEnunciado());
        assertEquals(gabaritoAntes, gravado.getGabaritoJson()); // o gabarito não muda
    }

    @Test
    void soAdminEditaConteudo() throws Exception {
        Usuario professor = usuarioRepository.save(ContasDeTeste.confirmada(new Usuario("Prof", "it.cont.prof",
                "it.cont.prof@teste.com", "hash", TipoUsuario.PROFESSOR)));
        mockMvc.perform(get("/api/admin/conteudo").with(como(professor))).andExpect(status().isForbidden());
        mockMvc.perform(put("/api/admin/exercicios/{id}", 1).with(como(professor)).with(csrfValido())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\": \"x\", \"enunciado\": \"x\", \"explicacao\": \"x\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/rodadas/{id}", 999_999).with(como(admin))).andExpect(status().isNotFound());
    }
}
