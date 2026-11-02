package br.com.logicup.backend.controller;

import br.com.logicup.backend.legal.VersoesLegais;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Sprint 9: limite de tentativas de ponta a ponta (HTTP -> Controller -> Service -> ProtecaoForcaBruta).
 *
 * Limites BAIXOS só neste teste (properties abaixo), para não precisar de dezenas de requisições.
 * Os outros testes usam limites altos (src/test/resources/config/application.properties).
 *
 * Os contadores ficam em MEMÓRIA e o Spring reaproveita o mesmo contexto entre os testes desta classe:
 * por isso cada teste usa um IP DIFERENTE (doIp) e contas próprias, e um teste não interfere no outro.
 */
@SpringBootTest(properties = {
        "logicup.limites.login-conta.maximo=3",
        "logicup.limites.login-ip.maximo=6",
        "logicup.limites.codigo-turma.maximo=3",
        "logicup.limites.cadastro.maximo=2",
})
@AutoConfigureMockMvc
@Transactional
class LimiteTentativasIntegrationTest {

    private static final String SENHA = "senhaForte123";

    @Autowired private MockMvc mockMvc;
    @Autowired private AlunoRepository alunoRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    // ---------------- helpers ----------------

    /** Simula a requisição vindo de um IP específico (no MockMvc o padrão seria sempre 127.0.0.1). */
    private static RequestPostProcessor doIp(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private Aluno aluno(String username) {
        return alunoRepository.save(new Aluno("Aluno Limite", username, username + "@teste.com",
                passwordEncoder.encode(SENHA)));
    }

    private ResultActions login(String login, String senha, String ip) throws Exception {
        return mockMvc.perform(post("/api/auth/login").with(csrfValido()).with(doIp(ip))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"login\": \"%s\", \"senha\": \"%s\"}".formatted(login, senha)));
    }

    private ResultActions cadastro(String username, String ip) throws Exception {
        return mockMvc.perform(post("/api/auth/cadastro").with(csrfValido()).with(doIp(ip))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nome": "Pessoa Limite", "username": "%s", "email": "%s@teste.com", "senha": "%s",
                         "perfil": "ALUNO", "aceiteTermos": true, "versaoTermos": "%s", "versaoPrivacidade": "%s"}
                        """.formatted(username, username, SENHA, VersoesLegais.TERMOS, VersoesLegais.PRIVACIDADE)));
    }

    private ResultActions entrarNaTurma(Aluno aluno, String codigo) throws Exception {
        RequestPostProcessor logado = jwt().jwt(token -> token.subject(String.valueOf(aluno.getId())).claim("role", "ALUNO")
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_ALUNO"));
        return mockMvc.perform(post("/api/minha-turma").with(logado).with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON).content("{\"codigo\":\"" + codigo + "\"}"));
    }

    // ---------------- login: regra "conta + IP" ----------------

    @Test
    void depoisDoMaximoDeErrosNemASenhaCertaEntra() throws Exception {
        aluno("it.lim.um");
        for (int i = 0; i < 3; i++) {
            login("it.lim.um", "senhaErrada", "10.0.0.1").andExpect(status().isUnauthorized());
        }
        // 4ª tentativa COM A SENHA CERTA: continua bloqueada (senão o atacante saberia que acertou)
        login("it.lim.um", SENHA, "10.0.0.1")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", matchesPattern("\\d+")))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.mensagem", containsString("Muitas tentativas de login")))
                .andExpect(cookie().doesNotExist(TokenService.NOME_COOKIE)); // e NÃO recebe o cookie de login
    }

    @Test
    void oBloqueioDaContaNumIpNaoTrancaAContaEmOutroIp() throws Exception {
        aluno("it.lim.dois");
        for (int i = 0; i < 3; i++) {
            login("it.lim.dois", "senhaErrada", "10.0.0.2").andExpect(status().isUnauthorized());
        }
        login("it.lim.dois", SENHA, "10.0.0.2").andExpect(status().isTooManyRequests());
        // D1-A: a dona da conta, em OUTRA rede, continua entrando normalmente
        login("it.lim.dois", SENHA, "10.0.0.3").andExpect(status().isOk());
    }

    @Test
    void loginCertoZeraOsErrosDaquelaContaNaqueleIp() throws Exception {
        aluno("it.lim.tres");
        login("it.lim.tres", "senhaErrada", "10.0.0.4").andExpect(status().isUnauthorized());
        login("it.lim.tres", "senhaErrada", "10.0.0.4").andExpect(status().isUnauthorized());
        login("it.lim.tres", SENHA, "10.0.0.4").andExpect(status().isOk()); // zera
        login("it.lim.tres", "senhaErrada", "10.0.0.4").andExpect(status().isUnauthorized());
        login("it.lim.tres", "senhaErrada", "10.0.0.4").andExpect(status().isUnauthorized()); // 2 erros, não 4
    }

    @Test
    void emailComMaiusculasContaComoAMesmaConta() throws Exception {
        aluno("it.lim.quatro");
        login("it.lim.quatro@teste.com", "senhaErrada", "10.0.0.6").andExpect(status().isUnauthorized());
        login("IT.LIM.QUATRO@teste.com", "senhaErrada", "10.0.0.6").andExpect(status().isUnauthorized());
        login(" It.Lim.Quatro@Teste.com ", "senhaErrada", "10.0.0.6").andExpect(status().isUnauthorized());
        login("it.lim.quatro@teste.com", SENHA, "10.0.0.6").andExpect(status().isTooManyRequests());
    }

    // ---------------- login: regra "só IP" ----------------

    @Test
    void quemTestaMuitasContasDiferentesEhBloqueadoPeloIp() throws Exception {
        // 6 contas diferentes (que nem existem), 1 erro em cada: nenhuma passa do limite da conta (3)...
        for (int i = 1; i <= 6; i++) {
            login("it.nao.existe" + i, "qualquer", "10.0.0.5").andExpect(status().isUnauthorized());
        }
        // ...mas o IP chegou a 6 erros: a próxima tentativa, em qualquer conta, é recusada
        login("it.nao.existe7", "qualquer", "10.0.0.5").andExpect(status().isTooManyRequests());
    }

    @Test
    void contaInexistenteRecebeOMesmoBloqueioQueContaExistente() throws Exception {
        // o limite não pode revelar se a conta existe: o comportamento é igual
        for (int i = 0; i < 3; i++) {
            login("it.ninguem@teste.com", "qualquer", "10.0.0.7").andExpect(status().isUnauthorized());
        }
        login("it.ninguem@teste.com", "qualquer", "10.0.0.7")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.mensagem", containsString("Muitas tentativas de login")));
    }

    // ---------------- código de turma ----------------

    @Test
    void muitosCodigosDeTurmaErradosBloqueiamSoAqueleAluno() throws Exception {
        Aluno chutador = aluno("it.lim.turma1");
        Aluno outro = aluno("it.lim.turma2");
        for (int i = 0; i < 3; i++) {
            entrarNaTurma(chutador, "ZZZZZZZ" + (i + 2)).andExpect(status().isNotFound());
        }
        entrarNaTurma(chutador, "ZZZZZZZ9")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.mensagem", containsString("códigos de turma incorretos")));
        // o limite é POR ALUNO: outro aluno não é afetado
        entrarNaTurma(outro, "ZZZZZZZ9").andExpect(status().isNotFound());
    }

    // ---------------- cadastro ----------------

    @Test
    void cadastroLimitadoPorIp() throws Exception {
        cadastro("it.lim.cad1", "10.0.0.9").andExpect(status().isCreated());
        cadastro("it.lim.cad2", "10.0.0.9").andExpect(status().isCreated());
        cadastro("it.lim.cad3", "10.0.0.9")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.mensagem", containsString("cadastro")));
        // outra rede continua podendo se cadastrar
        cadastro("it.lim.cad4", "10.0.0.10").andExpect(status().isCreated());
    }
}
