package br.com.logicup.backend.controller;

import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 8: aceite dos Termos de Uso e ciência da Política de Privacidade.
 *
 * Regras testadas aqui:
 *   - cadastro só com aceite ATIVO (aceiteTermos = true) e da versão VIGENTE; grava TERMOS e PRIVACIDADE;
 *   - quem tem conta e ainda não aceitou a versão vigente entra, mas fica "pendente": só pode ver /me,
 *     sair e aceitar. Qualquer outra rota responde 403 com codigo ACEITE_PENDENTE;
 *   - ao aceitar, o servidor grava o aceite (sem duplicar) e entrega um cookie de login novo.
 *
 * O aceite fica no TOKEN (claim assinada pelo servidor): o filtro não consulta o banco a cada requisição.
 * Login pelo caminho real (cookie de verdade), sem jwt(). O banco é conferido direto pela tabela
 * aceite_documento (JdbcTemplate), para o teste não depender de classes que ainda não existem.
 *
 * Precisa da tabela aceite_documento (migracao_s8_aceite_documento.sql, PASSO 1).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RecordApplicationEvents // Sprint 10: para ler o código do link de confirmação
class AceiteTermosIntegrationTest {

    /** Versão vigente dos dois documentos (minuta 0.1 da Luciana). */
    // Sprint 9: as versões vêm do próprio backend, para o teste não precisar mudar a cada versão nova dos textos
    private static final String VERSAO_TERMOS = VersoesLegais.TERMOS;
    private static final String VERSAO_PRIVACIDADE = VersoesLegais.PRIVACIDADE;
    private static final String SENHA = "senhaAceite123";

    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationEvents eventos;
    @Autowired private TokenService tokenService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JdbcTemplate jdbc;

    /** Aluno que já tinha conta ANTES dos Termos existirem: nenhum aceite no banco. */
    private Aluno antigo;

    @BeforeEach
    void criarAlunoAntigo() {
        antigo = alunoRepository.save(ContasDeTeste.confirmada(new Aluno("Aluna Antiga", "it.ac.antiga", "it.ac.antiga@teste.com",
                passwordEncoder.encode(SENHA))));
    }

    // ---------- apoio ----------

    private int aceites(Integer idUsuario, String documento, String versao) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM aceite_documento WHERE id_usuario = ? AND documento = ? AND versao = ?",
                Integer.class, idUsuario, documento, versao);
        return total == null ? 0 : total;
    }

    private int aceitesDoUsuario(Integer idUsuario) {
        Integer total = jdbc.queryForObject("SELECT COUNT(*) FROM aceite_documento WHERE id_usuario = ?",
                Integer.class, idUsuario);
        return total == null ? 0 : total;
    }

    /** O cookie de login que o servidor mandou no Set-Cookie (o mesmo que o navegador guardaria). */
    private static Cookie cookieDeLogin(MvcResult resultado) {
        for (String linha : resultado.getResponse().getHeaders("Set-Cookie")) {
            String[] nomeValor = linha.split(";")[0].split("=", 2);
            if (nomeValor[0].trim().equals(TokenService.NOME_COOKIE) && nomeValor.length > 1 && !nomeValor[1].isBlank()) {
                return new Cookie(TokenService.NOME_COOKIE, nomeValor[1].trim());
            }
        }
        return null;
    }

    private static String jsonCadastro(String username, String extras) {
        return """
                {"nome":"Aluna Nova","username":"%s","email":"%s@teste.com","senha":"%s","perfil":"ALUNO"%s}"""
                .formatted(username, username, SENHA, extras);
    }

    private ResultActions cadastrar(String username, String extras) throws Exception {
        return mockMvc.perform(post("/api/auth/cadastro").with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content(jsonCadastro(username, extras)));
    }

    private MvcResult loginDoAntigo() throws Exception {
        return mockMvc.perform(post("/api/auth/login").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\":\"it.ac.antiga\",\"senha\":\"" + SENHA + "\"}")).andReturn();
    }

    private ResultActions aceitar(Cookie login, String versaoTermos, String versaoPrivacidade) throws Exception {
        return mockMvc.perform(post("/api/legal/aceite").with(csrfValido()).cookie(login)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"versaoTermos\":\"%s\",\"versaoPrivacidade\":\"%s\"}".formatted(versaoTermos, versaoPrivacidade)));
    }

    private static final String ACEITE_OK = ",\"aceiteTermos\":true,\"versaoTermos\":\"" + VERSAO_TERMOS
            + "\",\"versaoPrivacidade\":\"" + VERSAO_PRIVACIDADE + "\"";

    // ---------- cadastro: aceite ATIVO e da versão vigente ----------

    @Test
    void cadastroSemAceiteERecusado() throws Exception {
        cadastrar("it.ac.sem", "")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.aceiteTermos").exists());
        assertFalse(usuarioRepository.existsByUsername("it.ac.sem"), "a conta não pode ser criada");
    }

    @Test
    void cadastroComAceiteFalsoERecusado() throws Exception {
        cadastrar("it.ac.falso", ",\"aceiteTermos\":false,\"versaoTermos\":\"" + VERSAO_TERMOS + "\",\"versaoPrivacidade\":\"" + VERSAO_PRIVACIDADE + "\"")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.aceiteTermos").exists());
        assertFalse(usuarioRepository.existsByUsername("it.ac.falso"));
    }

    @Test
    void cadastroComVersaoDesatualizadaERecusado() throws Exception {
        // a pessoa leu uma versão antiga (página aberta antes de uma atualização): precisa recarregar e ler a atual
        cadastrar("it.ac.velha", ",\"aceiteTermos\":true,\"versaoTermos\":\"0.0\",\"versaoPrivacidade\":\"" + VERSAO_PRIVACIDADE + "\"")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.campos.aceiteTermos").exists());
        assertFalse(usuarioRepository.existsByUsername("it.ac.velha"));
    }

    @Test
    void cadastroComAceiteGravaTermosEPrivacidadeNaVersaoVigente() throws Exception {
        cadastrar("it.ac.nova", ACEITE_OK).andExpect(status().isCreated());

        Integer id = usuarioRepository.findByUsername("it.ac.nova").orElseThrow().getId();
        assertEquals(1, aceites(id, "TERMOS", VERSAO_TERMOS));
        assertEquals(1, aceites(id, "PRIVACIDADE", VERSAO_PRIVACIDADE));
    }

    @Test
    void quemSeCadastraJaUsaAApiSemPendencia() throws Exception {
        cadastrar("it.ac.usa", ACEITE_OK).andExpect(status().isCreated());
        // Sprint 10: entra pelo link de confirmação; o aceite do cadastro já vale (sem janela de aceite)
        Cookie login = ContasDeTeste.confirmarCadastro(mockMvc, eventos, "it.ac.usa@teste.com");
        mockMvc.perform(get("/api/auth/me").cookie(login)).andExpect(jsonPath("$.aceitePendente").value(false));
        mockMvc.perform(get("/api/trilha").cookie(login)).andExpect(status().isOk());
    }

    // ---------- conta antiga, sem aceite: entra, mas fica pendente ----------

    @Test
    void contaAntigaEntraMasFicaPendente() throws Exception {
        MvcResult login = loginDoAntigo();
        assertEquals(200, login.getResponse().getStatus());
        mockMvc.perform(get("/api/auth/me").cookie(cookieDeLogin(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aceitePendente").value(true));
    }

    @Test
    void contaPendenteNaoUsaORestoDaApi() throws Exception {
        Cookie login = cookieDeLogin(loginDoAntigo());
        mockMvc.perform(get("/api/trilha").cookie(login))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACEITE_PENDENTE"));
        mockMvc.perform(get("/api/ranking").cookie(login))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACEITE_PENDENTE"));
    }

    @Test
    void contaPendentePodeSair() throws Exception {
        Cookie login = cookieDeLogin(loginDoAntigo());
        mockMvc.perform(post("/api/auth/logout").with(csrfValido()).cookie(login)).andExpect(status().isNoContent());
    }

    // ---------- aceitar ----------

    @Test
    void aceitarGravaOsDocumentosEEntregaCookieNovoQueLiberaAApi() throws Exception {
        Cookie loginPendente = cookieDeLogin(loginDoAntigo());

        MvcResult aceite = aceitar(loginPendente, VERSAO_TERMOS, VERSAO_PRIVACIDADE)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aceitePendente").value(false))
                .andReturn();

        assertEquals(1, aceites(antigo.getId(), "TERMOS", VERSAO_TERMOS));
        assertEquals(1, aceites(antigo.getId(), "PRIVACIDADE", VERSAO_PRIVACIDADE));

        Cookie loginNovo = cookieDeLogin(aceite);
        assertNotNull(loginNovo, "o aceite deve entregar um cookie de login novo (com o aceite dentro)");
        mockMvc.perform(get("/api/trilha").cookie(loginNovo)).andExpect(status().isOk());
    }

    @Test
    void aceitarVersaoDesatualizadaERecusado() throws Exception {
        Cookie login = cookieDeLogin(loginDoAntigo());
        aceitar(login, "0.0", VERSAO_PRIVACIDADE).andExpect(status().isConflict());
        assertEquals(0, aceitesDoUsuario(antigo.getId()), "nada pode ser gravado");
    }

    @Test
    void aceitarDuasVezesNaoDuplica() throws Exception {
        Cookie login = cookieDeLogin(loginDoAntigo());
        aceitar(login, VERSAO_TERMOS, VERSAO_PRIVACIDADE).andExpect(status().isOk());
        aceitar(login, VERSAO_TERMOS, VERSAO_PRIVACIDADE).andExpect(status().isOk());
        assertEquals(2, aceitesDoUsuario(antigo.getId()), "uma linha por documento, sem repetir");
    }

    @Test
    void tokenAntigoDeQuemJaAceitouPedeConfirmacaoSemDuplicar() throws Exception {
        // aceitou no banco, mas o token é de antes (sem o aceite dentro): confirma de novo e o banco não muda
        jdbc.update("INSERT INTO aceite_documento (id_usuario, documento, versao) VALUES (?, 'TERMOS', ?), (?, 'PRIVACIDADE', ?)",
                antigo.getId(), VERSAO_TERMOS, antigo.getId(), VERSAO_PRIVACIDADE);
        Cookie tokenAntigo = new Cookie(TokenService.NOME_COOKIE, tokenService.gerarToken(antigo));

        mockMvc.perform(get("/api/trilha").cookie(tokenAntigo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACEITE_PENDENTE"));
        aceitar(tokenAntigo, VERSAO_TERMOS, VERSAO_PRIVACIDADE).andExpect(status().isOk());
        assertEquals(2, aceitesDoUsuario(antigo.getId()));
    }

    @Test
    void aceitarSemLoginE401() throws Exception {
        mockMvc.perform(post("/api/legal/aceite").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"versaoTermos\":\"" + VERSAO_TERMOS + "\",\"versaoPrivacidade\":\"" + VERSAO_PRIVACIDADE + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
