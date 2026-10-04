package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.CorrecaoResponse;
import br.com.logicup.backend.dto.EnvioRespostasRequest;
import br.com.logicup.backend.dto.RespostaExercicioRequest;
import br.com.logicup.backend.dto.ResultadoExercicioResponse;
import br.com.logicup.backend.dto.ResumoAlunoResponse;
import br.com.logicup.backend.dto.ResumoEnvioResponse;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.exception.RodadaBloqueadaException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.ProgressoExercicio;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.StatusRodada;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ProgressoExercicioRepository;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.correcao.CorrecaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * POST /api/rodadas/{id}/respostas — ORQUESTRA o envio (D4).
 * As regras ficam nas entidades (ProgressoExercicio, Aluno) e em RegrasTrilha;
 * este Service decide a ORDEM dos passos e garante que tudo aconteça numa única transação.
 */
@Service
public class RespostaService {

    private final RodadaService rodadaService;
    private final TrilhaService trilhaService;
    private final ExercicioRepository exercicioRepository;
    private final ProgressoExercicioRepository progressoRepository;
    private final AlunoRepository alunoRepository;
    private final CorrecaoService correcaoService;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    public RespostaService(RodadaService rodadaService, TrilhaService trilhaService,
                           ExercicioRepository exercicioRepository, ProgressoExercicioRepository progressoRepository,
                           AlunoRepository alunoRepository, CorrecaoService correcaoService,
                           JsonMapper jsonMapper, Clock clock) {
        this.rodadaService = rodadaService;
        this.trilhaService = trilhaService;
        this.exercicioRepository = exercicioRepository;
        this.progressoRepository = progressoRepository;
        this.alunoRepository = alunoRepository;
        this.correcaoService = correcaoService;
        this.jsonMapper = jsonMapper;
        this.clock = clock;
    }

    /** @Transactional: se QUALQUER passo falhar, nada é gravado (nem XP, nem progresso). */
    @Transactional
    public CorrecaoResponse enviar(Integer rodadaId, UsuarioLogado usuario, EnvioRespostasRequest envio) {
        // 1) a rodada existe e está liberada para este aluno?
        Rodada rodada = rodadaService.buscarRodada(rodadaId);
        Map<Integer, StatusRodada> statusAntes = trilhaService.calcularSituacao(usuario).statusPorRodada();
        if (statusAntes.get(rodadaId) == StatusRodada.BLOQUEADA) {
            throw new RodadaBloqueadaException();
        }

        Aluno aluno = alunoRepository.findById(usuario.id()).orElseThrow(SessaoInvalidaException::new);

        // 2) cada exercício enviado pertence a esta rodada, e sem repetição?
        Map<Integer, Exercicio> exerciciosDaRodada = exercicioRepository.findByRodadaIdOrderByOrdenacaoAsc(rodadaId)
                .stream().collect(Collectors.toMap(Exercicio::getId, Function.identity()));
        validarEnvio(envio, exerciciosDaRodada);

        Map<Integer, ProgressoExercicio> progressoPorExercicio =
                progressoRepository.findByAlunoIdAndExercicioRodadaId(aluno.getId(), rodadaId).stream()
                        .collect(Collectors.toMap(progresso -> progresso.getExercicio().getId(), Function.identity()));

        // 3) corrige e registra cada resposta
        List<ResultadoExercicioResponse> resultados = new ArrayList<>();
        int acertos = 0;
        int xpDoEnvio = 0;
        for (RespostaExercicioRequest item : envio.respostas()) {
            Exercicio exercicio = exerciciosDaRodada.get(item.exercicioId());
            boolean acertou = correcaoService.corrigir(exercicio, item.resposta());

            ProgressoExercicio progresso = progressoPorExercicio.computeIfAbsent(
                    exercicio.getId(), id -> new ProgressoExercicio(aluno, exercicio));
            int xp = progresso.registrarResposta(jsonMapper.writeValueAsString(item.resposta()), acertou); // PR1-PR3
            progressoRepository.save(progresso);
            aluno.adicionarPontos(xp); // T1: xp só é > 0 no PRIMEIRO acerto da questão

            if (acertou) {
                acertos++;
            }
            xpDoEnvio += xp;
            resultados.add(new ResultadoExercicioResponse(exercicio.getId(), acertou, xp,
                    exercicio.getExplicacao(), correcaoService.respostaCorreta(exercicio)));
        }

        // 4) streak: todo envio conta como atividade (A4), no "hoje" de São Paulo
        LocalDate hoje = LocalDate.now(clock);
        aluno.registrarAtividade(hoje);

        // 5) recalcula a trilha com o progresso novo (aprovação - PR5 - e desbloqueios)
        TrilhaService.SituacaoTrilha depois = trilhaService.calcularSituacao(usuario);
        boolean aprovada = depois.statusDa(rodadaId) == StatusRodada.APROVADA;
        Integer proximaLiberada = depois.rodadas().stream()
                .map(Rodada::getId)
                .filter(id -> statusAntes.get(id) == StatusRodada.BLOQUEADA
                        && depois.statusDa(id) != StatusRodada.BLOQUEADA)
                .findFirst()
                .orElse(null);

        // 6) nível = fase mais alta liberada (N-A); a entidade garante que nunca diminui
        aluno.atualizarNivel(depois.faseMaisAltaLiberada());
        // o aluno não precisa de save(): é uma entidade gerenciada e o Hibernate grava as mudanças no fim da transação

        List<Exercicio> todosDaRodada = depois.exerciciosDa(rodada.getId());
        int concluidosNaRodada = (int) todosDaRodada.stream()
                .filter(exercicio -> depois.exerciciosConcluidos().contains(exercicio.getId()))
                .count();

        ResumoEnvioResponse resumo = new ResumoEnvioResponse(acertos, envio.respostas().size(), xpDoEnvio,
                concluidosNaRodada, todosDaRodada.size(), aprovada, proximaLiberada,
                ResumoAlunoResponse.de(aluno, hoje));
        return new CorrecaoResponse(resultados, resumo);
    }

    private static void validarEnvio(EnvioRespostasRequest envio, Map<Integer, Exercicio> exerciciosDaRodada) {
        Set<Integer> vistos = new HashSet<>();
        for (RespostaExercicioRequest item : envio.respostas()) {
            if (!exerciciosDaRodada.containsKey(item.exercicioId())) {
                throw new RequisicaoInvalidaException(
                        "O exercício " + item.exercicioId() + " não pertence a esta rodada.");
            }
            if (!vistos.add(item.exercicioId())) {
                throw new RequisicaoInvalidaException(
                        "O exercício " + item.exercicioId() + " foi enviado mais de uma vez.");
            }
        }
    }
}
