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
import br.com.logicup.backend.repository.RodadaRepository;
import br.com.logicup.backend.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static br.com.logicup.backend.controller.CsrfTeste.csrfValido;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sprint 10 (Etapa 5): a OFENSIVA (dias seguidos estudando) testada de ponta a ponta, pela API,
 * com um RELÓGIO CONTROLADO pelo teste.
 *
 * Por que um relógio de teste? A ofensiva depende de "que dia é hoje" (no fuso de São Paulo). Sem controlar
 * o relógio, o teste teria que esperar dias de verdade. Aqui trocamos o bean Clock por um que o teste
 * "adianta" (@Primary: quando há dois beans do mesmo tipo, o Spring injeta o marcado como principal).
 * Isso só é possível porque o sistema inteiro pega a hora do bean Clock, e não de LocalDate.now() direto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OfensivaIntegrationTest {

    private static final ZoneId SP = ZoneId.of("America/Sao_Paulo");

    /** Relógio que o teste muda de dia. */
    static final class RelogioDeTeste extends Clock {
        private volatile Instant agora = Instant.now();
        void irPara(LocalDateTime horaEmSaoPaulo) { agora = horaEmSaoPaulo.atZone(SP).toInstant(); }
        @Override public ZoneId getZone() { return SP; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return agora; }
    }

    @TestConfiguration
    static class Relogio {
        @Bean
        @Primary
        RelogioDeTeste relogioDeTeste() {
            return new RelogioDeTeste();
        }
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private RelogioDeTeste relogio;
    @Autowired private ModuloRepository moduloRepository;
    @Autowired private RodadaRepository rodadaRepository;
    @Autowired private ExercicioRepository exercicioRepository;
    @Autowired private AlunoRepository alunoRepository;

    private Rodada rodada;
    private Exercicio questao;
    private Aluno aluno;

    @BeforeEach
    void preparar() {
        // fase própria com ordenação negativa: vem antes da Fase 1 real, então a rodada já está liberada
        Modulo fase = moduloRepository.save(new Modulo("Fase de teste da ofensiva", "Teste", -300));
        rodada = rodadaRepository.save(new Rodada(fase, "Rodada da ofensiva", 1,
                "{\"objetivos\":[\"o\"],\"teoria\":[{\"titulo\":\"T\",\"texto\":\"t\"}],\"exemplos\":[]}"));
        questao = new Exercicio(rodada, "IT.OF.Q1", "Questão", 1, TipoExercicio.MULTIPLA_ESCOLHA,
                FormatoResposta.ESCOLHA_UNICA, 10, "Enunciado", "{\"correta\":\"a\"}", "Explicação");
        questao.setAlternativasJson("[{\"id\":\"a\",\"texto\":\"Sim\"},{\"id\":\"b\",\"texto\":\"Não\"}]");
        questao = exercicioRepository.save(questao);
        aluno = alunoRepository.save(new Aluno("Aluna Ofensiva", "it.ofensiva", "it.ofensiva@teste.com", "hash"));
    }

    private RequestPostProcessor comoAluno() {
        return jwt().jwt(t -> t.subject(String.valueOf(aluno.getId())).claim("role", "ALUNO")
                        .claim(TokenService.CLAIM_ACEITE, VersoesLegais.IDENTIFICADOR))
                .authorities(new SimpleGrantedAuthority("ROLE_ALUNO"));
    }

    /** Envia a rodada (certa ou errada: TODO envio conta como dia de estudo, regra A4). */
    private ResultActions estudar(String alternativa) throws Exception {
        return mockMvc.perform(post("/api/rodadas/{id}/respostas", rodada.getId()).with(comoAluno()).with(csrfValido())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"respostas\":[{\"exercicioId\":%d,\"resposta\":{\"alternativa\":\"%s\"}}]}"
                        .formatted(questao.getId(), alternativa)));
    }

    private ResultActions trilha() throws Exception {
        return mockMvc.perform(get("/api/trilha").with(comoAluno()));
    }

    private void dia(int dia, int hora) {
        relogio.irPara(LocalDateTime.of(2026, 11, dia, hora, 0));
    }

    @Test
    void ofensivaSobeComDiasSeguidosZeraQuandoPulaUmDiaERendeConquista() throws Exception {
        dia(2, 10);
        trilha().andExpect(jsonPath("$.aluno.streak").value(0));          // nunca estudou
        estudar("b").andExpect(status().isOk())                            // errou, mas estudou
                .andExpect(jsonPath("$.resumo.aluno.streak").value(1));

        dia(2, 22);                                                        // mesmo dia, de novo
        estudar("a").andExpect(jsonPath("$.resumo.aluno.streak").value(1));

        dia(3, 0);                                                         // 00h de São Paulo = dia seguinte
        trilha().andExpect(jsonPath("$.aluno.streak").value(1));           // vale até o fim do dia seguinte
        estudar("a").andExpect(jsonPath("$.resumo.aluno.streak").value(2));

        dia(4, 9);
        estudar("a").andExpect(jsonPath("$.resumo.aluno.streak").value(3));
        // 3 dias seguidos = conquista "Ofensiva" bronze, que aparece na lista de novas
        mockMvc.perform(get("/api/conquistas/novas").with(comoAluno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo", hasItem("OFENSIVA")));

        dia(6, 9);                                                         // pulou o dia 5
        trilha().andExpect(jsonPath("$.aluno.streak").value(0));           // a sequência quebrou
        estudar("a").andExpect(jsonPath("$.resumo.aluno.streak").value(1)); // e recomeça
    }
}
