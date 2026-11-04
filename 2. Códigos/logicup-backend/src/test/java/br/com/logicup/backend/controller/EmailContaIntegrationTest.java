package br.com.logicup.backend.controller;

import br.com.logicup.backend.agendamento.LimpezaDeTokens;
import br.com.logicup.backend.email.TipoEmailConta;
import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import br.com.logicup.backend.service.TokenUsuarioService;
import jakarta.persistence.EntityManager;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 10: confirmação de e-mail, reenvio do link, "esqueci minha senha" e boas-vindas.
 *
 * Os e-mails são lidos como EVENTOS (ver ContasDeTeste): nenhum e-mail sai de verdade nos testes.
 * Cada teste usa e-mails próprios ("it.em.*") e tudo é desfeito no final (@Transactional).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@RecordApplicationEvents
class EmailContaIntegrationTest {

    private static final String SENHA = "senhaForte123";
    private static final String NOVA_SENHA = "outraSenha456";

    @Autowired private MockMvc mockMvc;
    @Autowired private ApplicationEvents eventos;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private LimpezaDeTokens limpeza;

    // ---------------- helpers ----------------

    private ResultActions cadastrar(String username) throws Exception {
        return mockMvc.perform(post("/api/auth/cadastro").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "Bia Souza Lima", "username": "%s", "email": "%s@teste.com", "senha": "%s",
                         "perfil": "ALUNO", "aceiteTermos": true, "versaoTermos": "%s", "versaoPrivacidade": "%s"}
                        """.formatted(username, username, SENHA, VersoesLegais.TERMOS, VersoesLegais.PRIVACIDADE)));
    }

    private ResultActions login(String login, String senha) throws Exception {
        return mockMvc.perform(post("/api/auth/login").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\": \"%s\", \"senha\": \"%s\"}".formatted(login, senha)));
    }

    private ResultActions postEmail(String rota, String email) throws Exception {
        return mockMvc.perform(post(rota).with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"));
    }

    private ResultActions redefinir(String codigo, String novaSenha) throws Exception {
        return mockMvc.perform(post("/api/auth/redefinir-senha").with(csrfValido()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"%s\", \"novaSenha\": \"%s\"}".formatted(codigo, novaSenha)));
    }

    private int pedidos(TipoEmailConta tipo, String email) {
        return ContasDeTeste.pedidos(eventos, tipo, email).size();
    }

    private Aluno alunoConfirmado(String username) {
        return alunoRepository.save(ContasDeTeste.confirmada(new Aluno("Aluno Email", username, username + "@teste.com",
                passwordEncoder.encode(SENHA))));
    }

    // ---------------- confirmação ----------------

    @Test
    void loginSemConfirmarDevolve403ComCodigo() throws Exception {
        cadastrar("it.em.pendente").andExpect(status().isCreated());

        login("it.em.pendente", SENHA)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("EMAIL_NAO_CONFIRMADO"));
        // senha errada continua sendo o 401 genérico: quem chuta senha não descobre que a conta existe
        login("it.em.pendente", "senhaErrada99")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").doesNotExist());
    }

    @Test
    void linkDeConfirmacaoSoFuncionaUmaVez() throws Exception {
        cadastrar("it.em.uma").andExpect(status().isCreated());
        String codigo = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.CONFIRMAR_EMAIL, "it.em.uma@teste.com");

        assertEquals(200, ContasDeTeste.abrirLinkDeConfirmacao(mockMvc, codigo).getResponse().getStatus());
        var segunda = ContasDeTeste.abrirLinkDeConfirmacao(mockMvc, codigo);
        assertEquals(400, segunda.getResponse().getStatus());
        assertTrue(segunda.getResponse().getContentAsString().contains("LINK_INVALIDO"));

        // depois de confirmar, o login normal funciona
        login("it.em.uma@teste.com", SENHA).andExpect(status().isOk());
    }

    @Test
    void linkInventadoOuVencidoEhRecusado() throws Exception {
        assertEquals(400, ContasDeTeste.abrirLinkDeConfirmacao(mockMvc, "codigo-que-nao-existe").getResponse().getStatus());

        cadastrar("it.em.vencido").andExpect(status().isCreated());
        String codigo = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.CONFIRMAR_EMAIL, "it.em.vencido@teste.com");
        // simula a passagem do tempo: o link "venceu" há um minuto
        jdbc.update("UPDATE token_usuario SET expira_em = NOW() - INTERVAL 1 MINUTE WHERE hash_token = ?",
                TokenUsuarioService.hash(codigo));
        entityManager.clear(); // o Hibernate esquece a cópia em memória e relê do banco (como numa requisição nova)

        assertEquals(400, ContasDeTeste.abrirLinkDeConfirmacao(mockMvc, codigo).getResponse().getStatus());
        assertFalse(usuarioRepository.findByUsername("it.em.vencido").orElseThrow().isEmailConfirmado());
    }

    @Test
    void oBancoGuardaSoOHashDoCodigo() throws Exception {
        cadastrar("it.em.hash").andExpect(status().isCreated());
        String codigo = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.CONFIRMAR_EMAIL, "it.em.hash@teste.com");

        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM token_usuario WHERE hash_token = ?", Integer.class, codigo));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM token_usuario WHERE hash_token = ?", Integer.class,
                TokenUsuarioService.hash(codigo)));
        assertTrue(codigo.length() >= 43, "32 bytes aleatórios em Base64 = 43 caracteres");
    }

    // ---------------- reenvio ----------------

    @Test
    void reenvioInvalidaOLinkAnteriorERespondeIgualParaQualquerEmail() throws Exception {
        cadastrar("it.em.reenvio").andExpect(status().isCreated());
        String primeiro = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.CONFIRMAR_EMAIL, "it.em.reenvio@teste.com");

        postEmail("/api/auth/reenviar-confirmacao", "IT.Em.Reenvio@teste.com").andExpect(status().isAccepted());
        String segundo = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.CONFIRMAR_EMAIL, "it.em.reenvio@teste.com");
        assertNotEquals(primeiro, segundo);

        assertEquals(400, ContasDeTeste.abrirLinkDeConfirmacao(mockMvc, primeiro).getResponse().getStatus(),
                "só o link mais recente vale");
        assertEquals(200, ContasDeTeste.abrirLinkDeConfirmacao(mockMvc, segundo).getResponse().getStatus());

        // e-mail sem conta e conta já confirmada: a MESMA resposta, e nenhum e-mail sai
        postEmail("/api/auth/reenviar-confirmacao", "it.em.ninguem@teste.com").andExpect(status().isAccepted());
        postEmail("/api/auth/reenviar-confirmacao", "it.em.reenvio@teste.com").andExpect(status().isAccepted());
        assertEquals(0, pedidos(TipoEmailConta.CONFIRMAR_EMAIL, "it.em.ninguem@teste.com"));
        assertEquals(2, pedidos(TipoEmailConta.CONFIRMAR_EMAIL, "it.em.reenvio@teste.com"));
    }

    @Test
    void boasVindasSaiSoNaPrimeiraConfirmacao() throws Exception {
        cadastrar("it.em.bv").andExpect(status().isCreated());
        ContasDeTeste.confirmarCadastro(mockMvc, eventos, "it.em.bv@teste.com");
        assertEquals(1, pedidos(TipoEmailConta.BOAS_VINDAS, "it.em.bv@teste.com"));
        var pedido = ContasDeTeste.pedidos(eventos, TipoEmailConta.BOAS_VINDAS, "it.em.bv@teste.com").getFirst();
        assertEquals("Bia Souza Lima", pedido.nome());
        assertNull(pedido.codigo(), "boas-vindas não leva código secreto");
    }

    // ---------------- esqueci minha senha ----------------

    @Test
    void esqueciASenhaTrocaASenhaComUmLinkDeUsoUnico() throws Exception {
        alunoConfirmado("it.em.senha");

        postEmail("/api/auth/esqueci-senha", "it.em.senha@teste.com").andExpect(status().isAccepted());
        String codigo = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.REDEFINIR_SENHA, "it.em.senha@teste.com");

        redefinir(codigo, "curta").andExpect(status().isBadRequest()); // mesma regra de senha do cadastro
        redefinir(codigo, NOVA_SENHA).andExpect(status().isNoContent());
        redefinir(codigo, "maisUmaSenha789").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("LINK_INVALIDO"));

        login("it.em.senha", SENHA).andExpect(status().isUnauthorized());
        login("it.em.senha", NOVA_SENHA).andExpect(status().isOk());
    }

    @Test
    void esqueciASenhaRespondeIgualSemConta() throws Exception {
        postEmail("/api/auth/esqueci-senha", "it.em.fantasma@teste.com").andExpect(status().isAccepted());
        assertEquals(0, pedidos(TipoEmailConta.REDEFINIR_SENHA, "it.em.fantasma@teste.com"));
    }

    @Test
    void linkDeSenhaNaoServeParaConfirmarEmailENemOContrario() throws Exception {
        cadastrar("it.em.troca").andExpect(status().isCreated());
        String confirmacao = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.CONFIRMAR_EMAIL, "it.em.troca@teste.com");
        redefinir(confirmacao, NOVA_SENHA).andExpect(status().isBadRequest());

        postEmail("/api/auth/esqueci-senha", "it.em.troca@teste.com").andExpect(status().isAccepted());
        String senha = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.REDEFINIR_SENHA, "it.em.troca@teste.com");
        assertEquals(400, ContasDeTeste.abrirLinkDeConfirmacao(mockMvc, senha).getResponse().getStatus());
    }

    @Test
    void redefinirASenhaTambemConfirmaOEmail() throws Exception {
        cadastrar("it.em.naoconf").andExpect(status().isCreated());
        postEmail("/api/auth/esqueci-senha", "it.em.naoconf@teste.com").andExpect(status().isAccepted());
        String codigo = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.REDEFINIR_SENHA, "it.em.naoconf@teste.com");

        redefinir(codigo, NOVA_SENHA).andExpect(status().isNoContent());

        assertTrue(usuarioRepository.findByUsername("it.em.naoconf").orElseThrow().isEmailConfirmado(),
                "o link chegou no e-mail: isso prova que a pessoa é dona dele");
        var resposta = login("it.em.naoconf", NOVA_SENHA).andExpect(status().isOk()).andReturn();
        assertNotNull(resposta.getResponse().getCookie(TokenService.NOME_COOKIE));
    }

    @Test
    void rotasDeEmailSemCsrfSaoBloqueadas() throws Exception {
        mockMvc.perform(post("/api/auth/esqueci-senha").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"it.em.csrf@teste.com\"}")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/auth/confirmar-email").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"x\"}")).andExpect(status().isForbidden());
    }

    // ---------------- limpeza diária ----------------

    @Test
    void limpezaApagaSoOsCodigosVencidosHaMaisDe30Dias() throws Exception {
        cadastrar("it.em.limpa1").andExpect(status().isCreated());
        cadastrar("it.em.limpa2").andExpect(status().isCreated());
        String velho = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.CONFIRMAR_EMAIL, "it.em.limpa1@teste.com");
        String recente = ContasDeTeste.ultimoCodigo(eventos, TipoEmailConta.CONFIRMAR_EMAIL, "it.em.limpa2@teste.com");
        jdbc.update("UPDATE token_usuario SET expira_em = NOW() - INTERVAL 31 DAY WHERE hash_token = ?",
                TokenUsuarioService.hash(velho));
        jdbc.update("UPDATE token_usuario SET expira_em = NOW() - INTERVAL 29 DAY WHERE hash_token = ?",
                TokenUsuarioService.hash(recente));
        entityManager.clear();

        limpeza.executar();

        String conta = "SELECT COUNT(*) FROM token_usuario WHERE hash_token = ?";
        assertEquals(0, jdbc.queryForObject(conta, Integer.class, TokenUsuarioService.hash(velho)));
        assertEquals(1, jdbc.queryForObject(conta, Integer.class, TokenUsuarioService.hash(recente)),
                "vencido há 29 dias ainda fica (a folga é de 30)");
        assertTrue(usuarioRepository.findByUsername("it.em.limpa1").isPresent(), "a limpeza nunca apaga contas");
    }
}
