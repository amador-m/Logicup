package br.com.logicup.backend.controller;

import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.Modulo;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.TipoExercicio;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ModuloRepository;
import br.com.logicup.backend.repository.ProgressoExercicioRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static br.com.logicup.backend.controller.CsrfTeste.CABECALHO_CSRF;
import static br.com.logicup.backend.controller.CsrfTeste.NOME_COOKIE_CSRF;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tarefa 6 (segurança): CSRF nas rotas AUTENTICADAS, testado pelo caminho REAL de produção.
 *
 * Diferença para os outros testes de integração: aqui NÃO usamos o jwt() do spring-security-test.
 * O jwt() injeta o usuário direto no contexto de segurança (sem cookie) e ainda marca a requisição
 * para o CsrfFilter pular a checagem. Aqui o login vai num COOKIE de verdade, com um JWT assinado
 * pelo TokenService, exatamente como o navegador faz. Assim a requisição atravessa a cadeia de filtros
 * completa: CsrfFilter -> CookieTokenResolver -> autenticação JWT -> autorização -> Controller.
 *
 * ETAPA 1 (antes da correção): os testes marcados como "ATAQUE" FALHAM de propósito, porque hoje o
 * resource server do Spring dispensa o CSRF sempre que encontra o cookie de login
 * (spring-security issue #17959). Os testes "LEGÍTIMO" passam antes e depois da correção.
 *
 * @Transactional: tudo o que for gravado é desfeito no fim de cada teste.
 */
// A origem confiável é fixada AQUI, para o teste não depender de uma variável ORIGENS_CONFIAVEIS
// que exista na máquina de quem roda (a propriedade é criada na etapa 3; antes dela, é ignorada).
@SpringBootTest(properties = "logicup.seguranca.origens-confiaveis=" + SegurancaRequisicoesIntegrationTest.ORIGEM_CONFIAVEL)
@AutoConfigureMockMvc
@Transactional
class SegurancaRequisicoesIntegrationTest {

    static final String ORIGEM_CONFIAVEL = "http://localhost:5173";
    private static final String ORIGEM_MALICIOSA = "https://site-malicioso.com";
    private static final String MENSAGEM_ORIGEM = "Origem da requisição não permitida.";

    private static final String SENHA = "senhaSegura123";
    private static final String TOKEN_CSRF = "token-csrf-do-teste-de-seguranca";
    private static final String MENSAGEM_CSRF = "Token CSRF ausente ou inválido. Recarregue a página e tente novamente.";

    @Autowired private MockMvc mockMvc;
    @Autowired private TokenService tokenService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private ProgressoExercicioRepository progressoRepository;

    private Aluno aluno;
    private Cookie cookieDeLogin;   // o mesmo cookie que o navegador guarda depois do login
    private Rodada rodada;
    private Exercicio questao;

    @BeforeEach
    void preparar() {
        aluno = alunoRepository.save(new Aluno("Aluna Segurança", "it.seg.eu", "it.seg.eu@teste.com",
                passwordEncoder.encode(SENHA)));
        cookieDeLogin = new Cookie(TokenService.NOME_COOKIE, tokenService.gerarToken(aluno, true)); // com o aceite dos Termos

        // fase própria com ordenação negativa: vem ANTES da Fase 1 real, então a rodada 1 já está liberada
        Modulo fase = moduloRepository.save(new Modulo("Fase de teste de segurança", "Teste", -400));
        rodada = rodadaRepository.save(new Rodada(fase, "Rodada de segurança", 1,
                "{\"objetivos\":[],\"teoria\":[],\"exemplos\":[]}"));
        questao = new Exercicio(rodada, "IT.SEG.Q1", "Questão de segurança", 1, TipoExercicio.MULTIPLA_ESCOLHA,
                FormatoResposta.ESCOLHA_UNICA, 10, "Enunciado", "{\"correta\":\"a\"}", "Explicação");
        questao.setAlternativasJson("[{\"id\":\"a\",\"texto\":\"Sim\"},{\"id\":\"b\",\"texto\":\"Não\"}]");
        questao = exercicioRepository.save(questao);
    }

    // ---------- montagem das requisições ----------

    private static final String JSON_PERFIL = "{\"nome\":\"Nome Trocado Pelo Atacante\",\"username\":\"it.seg.hacker\"}";
    private static final String JSON_SENHA = "{\"senhaAtual\":\"" + SENHA + "\",\"novaSenha\":\"senhaDoAtacante1\"}";

    private String jsonRespostas() {
        return "{\"respostas\":[{\"exercicioId\":%d,\"resposta\":{\"alternativa\":\"a\"}}]}".formatted(questao.getId());
    }

    /** Variações do token CSRF que um atacante (ou um bug) poderia produzir. */
    private enum Csrf { NENHUM, SO_COOKIE, SO_CABECALHO, DIFERENTES, VALIDO }

    private MockHttpServletRequestBuilder comLoginReal(MockHttpServletRequestBuilder requisicao, Csrf csrf) {
        requisicao.cookie(cookieDeLogin).contentType(MediaType.APPLICATION_JSON);
        switch (csrf) {
            case NENHUM -> { }
            case SO_COOKIE -> requisicao.cookie(new Cookie(NOME_COOKIE_CSRF, TOKEN_CSRF));
            case SO_CABECALHO -> requisicao.header(CABECALHO_CSRF, TOKEN_CSRF);
            case DIFERENTES -> requisicao.cookie(new Cookie(NOME_COOKIE_CSRF, TOKEN_CSRF))
                    .header(CABECALHO_CSRF, "valor-forjado-pelo-atacante");
            case VALIDO -> requisicao.cookie(new Cookie(NOME_COOKIE_CSRF, TOKEN_CSRF))
                    .header(CABECALHO_CSRF, TOKEN_CSRF);
        }
        return requisicao;
    }

    private ResultActions trocarPerfil(Csrf csrf) throws Exception {
        return mockMvc.perform(comLoginReal(put("/api/perfil"), csrf).content(JSON_PERFIL));
    }

    private ResultActions trocarSenha(Csrf csrf) throws Exception {
        return mockMvc.perform(comLoginReal(put("/api/perfil/senha"), csrf).content(JSON_SENHA));
    }

    private ResultActions enviarRespostas(Csrf csrf) throws Exception {
        return mockMvc.perform(comLoginReal(post("/api/rodadas/{id}/respostas", rodada.getId()), csrf)
                .content(jsonRespostas()));
    }

    /** Um ataque bloqueado tem que deixar os dados exatamente como estavam. */
    private void perfilContinuaIntacto() {
        var salvo = usuarioRepository.findById(aluno.getId()).orElseThrow();
        assertEquals("Aluna Segurança", salvo.getNome());
        assertEquals("it.seg.eu", salvo.getUsername());
    }

    /** Devolve o ResultActions para o teste poder encadear verificações extras. */
    private static ResultActions bloqueadoPorCsrf(ResultActions resposta) throws Exception {
        return resposta.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_CSRF));
    }

    // ---------- ATAQUE: PUT /api/perfil com o cookie de login real ----------

    @Test
    void ataquePerfilSemNenhumTokenCsrfEBloqueado() throws Exception {
        bloqueadoPorCsrf(trocarPerfil(Csrf.NENHUM));
        perfilContinuaIntacto();
    }

    @Test
    void ataquePerfilSoComOCookieCsrfEBloqueado() throws Exception {
        // ter o cookie NÃO basta: o navegador anexa cookies sozinho, inclusive num ataque
        bloqueadoPorCsrf(trocarPerfil(Csrf.SO_COOKIE));
        perfilContinuaIntacto();
    }

    @Test
    void ataquePerfilSoComOCabecalhoCsrfEBloqueado() throws Exception {
        bloqueadoPorCsrf(trocarPerfil(Csrf.SO_CABECALHO));
        perfilContinuaIntacto();
    }

    @Test
    void ataquePerfilComTokenDiferenteDoCookieEBloqueadoSemVazarOsValores() throws Exception {
        bloqueadoPorCsrf(trocarPerfil(Csrf.DIFERENTES))
                // a resposta de erro não pode ecoar nenhum dos tokens
                .andExpect(content().string(not(containsString(TOKEN_CSRF))))
                .andExpect(content().string(not(containsString("valor-forjado-pelo-atacante"))));
        perfilContinuaIntacto();
    }

    // ---------- ATAQUE: PUT /api/perfil/senha ----------

    @Test
    void ataqueTrocaDeSenhaSemCsrfEBloqueado() throws Exception {
        bloqueadoPorCsrf(trocarSenha(Csrf.NENHUM));

        String hash = usuarioRepository.findById(aluno.getId()).orElseThrow().getSenha();
        assertTrue(passwordEncoder.matches(SENHA, hash), "a senha antiga tem que continuar valendo");
    }

    @Test
    void ataqueTrocaDeSenhaComTokenDiferenteEBloqueado() throws Exception {
        bloqueadoPorCsrf(trocarSenha(Csrf.DIFERENTES));

        String hash = usuarioRepository.findById(aluno.getId()).orElseThrow().getSenha();
        assertTrue(passwordEncoder.matches(SENHA, hash));
    }

    // ---------- ATAQUE: POST /api/rodadas/{id}/respostas ----------

    @Test
    void ataqueEnvioDeRespostasSemCsrfEBloqueado() throws Exception {
        bloqueadoPorCsrf(enviarRespostas(Csrf.NENHUM));
        assertTrue(progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), questao.getId()).isEmpty(),
                "nenhum progresso pode ser gravado");
    }

    @Test
    void ataqueEnvioDeRespostasSoComOCookieCsrfEBloqueado() throws Exception {
        bloqueadoPorCsrf(enviarRespostas(Csrf.SO_COOKIE));
        assertTrue(progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), questao.getId()).isEmpty());
    }

    // ---------- LEGÍTIMO: com cookie de login real e token CSRF correto ----------

    @Test
    void legitimoPerfilComCsrfValidoFunciona() throws Exception {
        trocarPerfil(Csrf.VALIDO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Nome Trocado Pelo Atacante"));
    }

    @Test
    void legitimoTrocaDeSenhaComCsrfValidoFunciona() throws Exception {
        trocarSenha(Csrf.VALIDO).andExpect(status().isNoContent());
    }

    @Test
    void legitimoEnvioDeRespostasComCsrfValidoFunciona() throws Exception {
        enviarRespostas(Csrf.VALIDO)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultados[0].correta").value(true));
    }

    @Test
    void leituraNaoExigeCsrf() throws Exception {
        // GET não altera dados: continua funcionando só com o cookie de login
        mockMvc.perform(get("/api/auth/me").cookie(cookieDeLogin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("it.seg.eu"));
        mockMvc.perform(get("/api/trilha").cookie(cookieDeLogin)).andExpect(status().isOk());
    }

    @Test
    void semCookieDeLoginContinuaSendo401() throws Exception {
        mockMvc.perform(put("/api/perfil").contentType(MediaType.APPLICATION_JSON).content(JSON_PERFIL)
                        .cookie(new Cookie(NOME_COOKIE_CSRF, TOKEN_CSRF)).header(CABECALHO_CSRF, TOKEN_CSRF))
                .andExpect(status().isUnauthorized());
    }

    // =====================================================================================
    // ETAPA 3: VALIDAÇÃO DE ORIGEM
    // Todos os casos abaixo mandam CSRF VÁLIDO e o cookie de login real: o único problema (ou não)
    // está nos cabeçalhos de origem. Assim fica provado que a origem é uma camada INDEPENDENTE do CSRF.
    // Regra: todo cabeçalho presente (Sec-Fetch-Site, Origin, Referer) tem que ser compatível;
    // os ausentes não aprovam nem reprovam; Origin "null" é sempre reprovado; GET não é afetado.
    // =====================================================================================

    /** Cabeçalhos de origem de uma requisição; null = cabeçalho ausente. */
    private record Origem(String secFetchSite, String origin, String referer) {
    }

    private ResultActions trocarPerfilVindoDe(Origem origem) throws Exception {
        MockHttpServletRequestBuilder requisicao = comLoginReal(put("/api/perfil"), Csrf.VALIDO).content(JSON_PERFIL);
        if (origem.secFetchSite() != null) requisicao.header("Sec-Fetch-Site", origem.secFetchSite());
        if (origem.origin() != null) requisicao.header("Origin", origem.origin());
        if (origem.referer() != null) requisicao.header("Referer", origem.referer());
        return mockMvc.perform(requisicao);
    }

    private void bloqueadoPorOrigem(Origem origem) throws Exception {
        trocarPerfilVindoDe(origem)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ORIGEM));
        perfilContinuaIntacto();
    }

    private void permitidoPelaOrigem(Origem origem) throws Exception {
        trocarPerfilVindoDe(origem).andExpect(status().isOk());
    }

    // ---------- ATAQUE (origem): um cabeçalho só, e ele é ruim ----------

    @Test
    void origemMaliciosaEBloqueadaMesmoComCsrfValido() throws Exception {
        bloqueadoPorOrigem(new Origem(null, ORIGEM_MALICIOSA, null));
    }

    @Test
    void origemNullEBloqueada() throws Exception {
        // navegadores mandam "null" em contextos opacos (iframe sandbox, Referrer-Policy no-referrer...)
        bloqueadoPorOrigem(new Origem(null, "null", null));
    }

    @Test
    void origemComSufixoEnganosoEBloqueada() throws Exception {
        // comparação EXATA: "começa com localhost:5173" não serve
        bloqueadoPorOrigem(new Origem(null, ORIGEM_CONFIAVEL + ".evil.com", null));
    }

    @Test
    void origemComPortaDiferenteEBloqueada() throws Exception {
        bloqueadoPorOrigem(new Origem(null, "http://localhost:8000", null)); // mesmo SITE, outra ORIGEM
    }

    @Test
    void origemComEsquemaDiferenteEBloqueada() throws Exception {
        bloqueadoPorOrigem(new Origem(null, "https://localhost:5173", null));
    }

    @Test
    void secFetchSiteCrossSiteEBloqueado() throws Exception {
        bloqueadoPorOrigem(new Origem("cross-site", null, null));
    }

    @Test
    void secFetchSiteSameSiteEBloqueado() throws Exception {
        // same-site = outro subdomínio ou outra porta do mesmo site: não é a nossa origem
        bloqueadoPorOrigem(new Origem("same-site", null, null));
    }

    @Test
    void secFetchSiteNoneEBloqueadoEmRequisicaoQueAlteraDados() throws Exception {
        bloqueadoPorOrigem(new Origem("none", null, null));
    }

    @Test
    void refererMaliciosoEBloqueado() throws Exception {
        bloqueadoPorOrigem(new Origem(null, null, ORIGEM_MALICIOSA + "/pagina-do-ataque"));
    }

    // ---------- ATAQUE (origem): cabeçalhos em CONFLITO; um bom não compensa um ruim ----------

    @Test
    void conflitoSameOriginComOriginMaliciosoEBloqueado() throws Exception {
        bloqueadoPorOrigem(new Origem("same-origin", ORIGEM_MALICIOSA, null));
    }

    @Test
    void conflitoCrossSiteComOriginConfiavelEBloqueado() throws Exception {
        bloqueadoPorOrigem(new Origem("cross-site", ORIGEM_CONFIAVEL, null));
    }

    @Test
    void conflitoOriginConfiavelComRefererMaliciosoEBloqueado() throws Exception {
        bloqueadoPorOrigem(new Origem(null, ORIGEM_CONFIAVEL, ORIGEM_MALICIOSA + "/x"));
    }

    // ---------- ATAQUE (origem): rotas públicas e outras rotas que alteram dados ----------

    @Test
    void loginVindoDeOrigemMaliciosaEBloqueado() throws Exception {
        // "login CSRF": o atacante logaria a vítima na conta DELE
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"it.seg.eu\",\"senha\":\"" + SENHA + "\"}")
                        .cookie(new Cookie(NOME_COOKIE_CSRF, TOKEN_CSRF)).header(CABECALHO_CSRF, TOKEN_CSRF)
                        .header("Origin", ORIGEM_MALICIOSA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ORIGEM));
    }

    @Test
    void cadastroVindoDeOrigemMaliciosaEBloqueado() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Conta do Atacante","username":"it.seg.atacante","email":"it.seg.atacante@teste.com",
                                 "senha":"%s","perfil":"ALUNO","aceiteTermos":true,"versaoTermos":"%s","versaoPrivacidade":"%s"}""".formatted(SENHA, VersoesLegais.TERMOS, VersoesLegais.PRIVACIDADE))
                        .cookie(new Cookie(NOME_COOKIE_CSRF, TOKEN_CSRF)).header(CABECALHO_CSRF, TOKEN_CSRF)
                        .header("Origin", ORIGEM_MALICIOSA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ORIGEM));
        assertFalse(usuarioRepository.existsByUsername("it.seg.atacante"), "a conta não pode ser criada");
    }

    @Test
    void logoutVindoDeOrigemMaliciosaEBloqueado() throws Exception {
        mockMvc.perform(post("/api/auth/logout").cookie(cookieDeLogin)
                        .cookie(new Cookie(NOME_COOKIE_CSRF, TOKEN_CSRF)).header(CABECALHO_CSRF, TOKEN_CSRF)
                        .header("Origin", ORIGEM_MALICIOSA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ORIGEM));
    }

    @Test
    void envioDeRespostasVindoDeOrigemMaliciosaEBloqueado() throws Exception {
        mockMvc.perform(comLoginReal(post("/api/rodadas/{id}/respostas", rodada.getId()), Csrf.VALIDO)
                        .content(jsonRespostas()).header("Sec-Fetch-Site", "cross-site").header("Origin", ORIGEM_MALICIOSA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value(MENSAGEM_ORIGEM));
        assertTrue(progressoRepository.findByAlunoIdAndExercicioId(aluno.getId(), questao.getId()).isEmpty());
    }

    // ---------- LEGÍTIMO (origem) ----------

    @Test
    void navegadorNaMesmaOrigemEPermitido() throws Exception {
        // o que o Chrome/Firefox mandam quando o React (via proxy do Vite) chama a API
        permitidoPelaOrigem(new Origem("same-origin", ORIGEM_CONFIAVEL, ORIGEM_CONFIAVEL + "/perfil"));
    }

    @Test
    void soOriginConfiavelEPermitido() throws Exception {
        permitidoPelaOrigem(new Origem(null, ORIGEM_CONFIAVEL, null));
    }

    @Test
    void soSecFetchSiteSameOriginEPermitido() throws Exception {
        permitidoPelaOrigem(new Origem("same-origin", null, null));
    }

    @Test
    void soRefererConfiavelEPermitido() throws Exception {
        permitidoPelaOrigem(new Origem(null, null, ORIGEM_CONFIAVEL + "/perfil?aba=senha"));
    }

    @Test
    void semNenhumCabecalhoDeOrigemSegueParaOCsrf() throws Exception {
        // cliente fora do navegador (curl, Postman, testes): passa pela origem, mas o CSRF continua exigido
        permitidoPelaOrigem(new Origem(null, null, null));
        bloqueadoPorCsrf(trocarPerfil(Csrf.NENHUM)); // sem cabeçalhos de origem E sem CSRF -> 403 de CSRF
    }

    @Test
    void leituraNaoEAfetadaPelaOrigem() throws Exception {
        // GET não altera dados: a regra de origem não se aplica
        mockMvc.perform(get("/api/auth/me").cookie(cookieDeLogin)
                        .header("Sec-Fetch-Site", "cross-site").header("Origin", ORIGEM_MALICIOSA))
                .andExpect(status().isOk());
    }

    // ---------- LEGÍTIMO: fluxo completo, como o navegador faz ----------

    /**
     * "Navegador" mínimo: guarda os cookies que o servidor manda (e apaga os de validade 0)
     * e reenvia todos nas próximas requisições. Em POST/PUT, copia o cookie CSRF para o
     * cabeçalho, exatamente como o services/api.js do React.
     * Os cookies vêm SEMPRE das respostas do servidor: nada é inventado pelo teste.
     * Lê o cabeçalho Set-Cookie BRUTO (o que o navegador recebe), e não a lista interna getCookies()
     * do MockMvc: a lista interna guardava um cookie que tinha sumido do cabeçalho e escondia um bug.
     */
    private final Map<String, String> cookiesDoNavegador = new LinkedHashMap<>();

    private MvcResult navegar(MockHttpServletRequestBuilder requisicao, boolean alteraDados) throws Exception {
        cookiesDoNavegador.forEach((nome, valor) -> requisicao.cookie(new Cookie(nome, valor)));
        if (alteraDados && cookiesDoNavegador.containsKey(NOME_COOKIE_CSRF)) {
            requisicao.header(CABECALHO_CSRF, cookiesDoNavegador.get(NOME_COOKIE_CSRF));
        }
        MvcResult resultado = mockMvc.perform(requisicao).andReturn();
        for (String setCookie : resultado.getResponse().getHeaders("Set-Cookie")) {
            String[] partes = setCookie.split(";");
            String[] nomeValor = partes[0].split("=", 2);
            String nome = nomeValor[0].trim();
            String valor = nomeValor.length > 1 ? nomeValor[1].trim() : "";
            boolean apaga = valor.isEmpty();
            for (int i = 1; i < partes.length; i++) {
                if (partes[i].trim().equalsIgnoreCase("Max-Age=0")) apaga = true;
            }
            if (apaga) {
                cookiesDoNavegador.remove(nome);
            } else {
                cookiesDoNavegador.put(nome, valor);
            }
        }
        return resultado;
    }

    @Test
    void fluxoCompletoLegitimoCadastroPerfilRespostasLogout() throws Exception {
        // 1) o React abre o site e pede o token CSRF
        assertEquals(204, navegar(get("/api/auth/csrf"), false).getResponse().getStatus());
        assertNotNull(cookiesDoNavegador.get(NOME_COOKIE_CSRF), "o servidor tem que entregar o cookie CSRF");

        // 2) cadastro (rota pública): devolve o cookie de login
        MvcResult cadastro = navegar(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Aluna Fluxo","username":"it.seg.fluxo","email":"it.seg.fluxo@teste.com",
                 "senha":"%s","perfil":"ALUNO","aceiteTermos":true,"versaoTermos":"%s","versaoPrivacidade":"%s"}""".formatted(SENHA, VersoesLegais.TERMOS, VersoesLegais.PRIVACIDADE)), true);
        assertEquals(201, cadastro.getResponse().getStatus());
        assertNotNull(cookiesDoNavegador.get(TokenService.NOME_COOKIE), "o cadastro tem que deixar a pessoa logada");

        // 3) leitura, edição do perfil e envio de respostas, com os cookies e o token que o servidor entregou
        assertEquals(200, navegar(get("/api/auth/me"), false).getResponse().getStatus());
        assertEquals(200, navegar(put("/api/perfil").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Aluna Fluxo Editada\",\"username\":\"it.seg.fluxo\"}"), true)
                .getResponse().getStatus());
        assertEquals(200, navegar(post("/api/rodadas/{id}/respostas", rodada.getId())
                .contentType(MediaType.APPLICATION_JSON).content(jsonRespostas()), true)
                .getResponse().getStatus());

        // 4) logout: apaga o cookie de login
        assertEquals(204, navegar(post("/api/auth/logout"), true).getResponse().getStatus());
        assertFalse(cookiesDoNavegador.containsKey(TokenService.NOME_COOKIE), "o logout tem que apagar o cookie de login");

        // 5) depois do logout, a sessão acabou
        assertEquals(401, navegar(get("/api/auth/me"), false).getResponse().getStatus());
    }
}
