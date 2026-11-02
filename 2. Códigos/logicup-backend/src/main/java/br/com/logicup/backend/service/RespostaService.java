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
import br.com.logicup.backend.model.FormatoResposta;
import br.com.logicup.backend.model.ProgressoExercicio;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.StatusRodada;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ProgressoExercicioRepository;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.correcao.CorrecaoService;
import br.com.logicup.backend.service.correcao.ResultadoCorrecao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
 *
 * Sprint 9 (ADR-001): o envio agora tem DUAS fases.
 *   Fase 1 (fora da transação de escrita): executa as questões de CÓDIGO no Piston.
 *   Fase 2 (uma transação, como antes): corrige o resto, grava progresso, XP, streak e conquistas.
 * Por quê: executar pode levar alguns segundos (fila + execução). Dentro da transação, cada envio
 * seguraria uma conexão do banco esse tempo todo; com uma turma enviando junto, as conexões acabariam
 * e o site inteiro travaria. Executando antes, a transação continua curta.
 *
 * Por que TransactionTemplate e não @Transactional: o @Transactional funciona por um PROXY que o Spring
 * coloca em volta do bean. Uma chamada de um método para outro da MESMA classe não passa pelo proxy,
 * então "enviarNaTransacao" anotado com @Transactional e chamado daqui NÃO abriria transação.
 * O TransactionTemplate abre a transação de forma explícita, sem depender do proxy.
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
    private final ConquistaService conquistaService;
    private final Clock clock;
    private final TransactionTemplate transacao;
    private final TransactionTemplate leitura;

    public RespostaService(RodadaService rodadaService, TrilhaService trilhaService,
                           ExercicioRepository exercicioRepository, ProgressoExercicioRepository progressoRepository,
                           AlunoRepository alunoRepository, CorrecaoService correcaoService,
                           JsonMapper jsonMapper, ConquistaService conquistaService, Clock clock,
                           PlatformTransactionManager gerenciadorDeTransacoes) {
        this.rodadaService = rodadaService;
        this.trilhaService = trilhaService;
        this.exercicioRepository = exercicioRepository;
        this.progressoRepository = progressoRepository;
        this.alunoRepository = alunoRepository;
        this.correcaoService = correcaoService;
        this.jsonMapper = jsonMapper;
        this.conquistaService = conquistaService;
        this.clock = clock;
        this.transacao = new TransactionTemplate(gerenciadorDeTransacoes);
        this.leitura = new TransactionTemplate(gerenciadorDeTransacoes);
        this.leitura.setReadOnly(true);
    }

    public CorrecaoResponse enviar(Integer rodadaId, UsuarioLogado usuario, EnvioRespostasRequest envio) {
        Map<Integer, ResultadoCorrecao> correcoesDeCodigo = corrigirCodigoAntes(rodadaId, usuario, envio); // fase 1
        // fase 2: se QUALQUER passo falhar, nada é gravado (nem XP, nem progresso)
        return transacao.execute(status -> enviarNaTransacao(rodadaId, usuario, envio, correcoesDeCodigo));
    }

    /**
     * Fase 1. Primeiro, numa transação SÓ DE LEITURA e curta: a rodada existe, está liberada e as respostas
     * são desta rodada? (não gastamos execução com envio inválido). Depois, FORA de transação, executa o código.
     */
    private Map<Integer, ResultadoCorrecao> corrigirCodigoAntes(Integer rodadaId, UsuarioLogado usuario,
                                                                EnvioRespostasRequest envio) {
        List<Exercicio> deCodigo = leitura.execute(status -> {
            rodadaService.buscarRodada(rodadaId); // 404 se não existir
            if (trilhaService.calcularSituacao(usuario).statusPorRodada().get(rodadaId) == StatusRodada.BLOQUEADA) {
                throw new RodadaBloqueadaException();
            }
            Map<Integer, Exercicio> daRodada = exercicioRepository.findByRodadaIdOrderByOrdenacaoAsc(rodadaId)
                    .stream().collect(Collectors.toMap(Exercicio::getId, Function.identity()));
            validarEnvio(envio, daRodada);
            return envio.respostas().stream()
                    .map(item -> daRodada.get(item.exercicioId()))
                    .filter(exercicio -> exercicio.getFormatoResposta() == FormatoResposta.CODIGO)
                    .toList();
        });

        Map<Integer, ResultadoCorrecao> correcoes = new HashMap<>();
        for (Exercicio exercicio : deCodigo) {
            Object resposta = envio.respostas().stream()
                    .filter(item -> item.exercicioId().equals(exercicio.getId()))
                    .findFirst().orElseThrow().resposta();
            correcoes.put(exercicio.getId(), correcaoService.avaliar(exercicio, resposta));
        }
        return correcoes;
    }

    /** Fase 2: a lógica de sempre, agora recebendo prontas as correções de código da fase 1. */
    private CorrecaoResponse enviarNaTransacao(Integer rodadaId, UsuarioLogado usuario, EnvioRespostasRequest envio,
                                               Map<Integer, ResultadoCorrecao> correcoesDeCodigo) {
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
        int pendentes = 0;
        for (RespostaExercicioRequest item : envio.respostas()) {
            Exercicio exercicio = exerciciosDaRodada.get(item.exercicioId());
            ResultadoCorrecao correcao = correcoesDeCodigo.containsKey(exercicio.getId())
                    ? correcoesDeCodigo.get(exercicio.getId())
                    : correcaoService.avaliar(exercicio, item.resposta());

            // Sprint 9 (decisão 1-A): PENDENTE não grava nada (nem tentativa, nem XP) e não mostra a resposta modelo
            if (correcao.pendente()) {
                pendentes++;
                resultados.add(ResultadoExercicioResponse.pendente(exercicio.getId(), correcao.detalhe()));
                continue;
            }
            boolean acertou = correcao.correta();

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
                    exercicio.getExplicacao(), correcaoService.respostaCorreta(exercicio), false, correcao.detalhe()));
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

        // 7) Sprint 9 (B1): conquistas novas + gemas, na MESMA transação do XP (ou grava tudo, ou nada).
        //    O frontend descobre o que comemorar pelo GET /api/conquistas/novas.
        conquistaService.avaliar(aluno, depois);

        List<Exercicio> todosDaRodada = depois.exerciciosDa(rodada.getId());
        int concluidosNaRodada = (int) todosDaRodada.stream()
                .filter(exercicio -> depois.exerciciosConcluidos().contains(exercicio.getId()))
                .count();

        ResumoEnvioResponse resumo = new ResumoEnvioResponse(acertos, envio.respostas().size(), xpDoEnvio,
                concluidosNaRodada, todosDaRodada.size(), aprovada, proximaLiberada,
                ResumoAlunoResponse.de(aluno, hoje), pendentes);
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
