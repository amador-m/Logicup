package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.FaseResponse;
import br.com.logicup.backend.dto.ResumoAlunoResponse;
import br.com.logicup.backend.dto.RodadaResumoResponse;
import br.com.logicup.backend.dto.TrilhaResponse;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.Modulo;
import br.com.logicup.backend.model.ProgressoExercicio;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.StatusRodada;
import br.com.logicup.backend.model.TipoExercicio;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ProgressoExercicioRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import br.com.logicup.backend.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Monta a situação da trilha de um usuário: rodadas em ordem, o que ele concluiu e o status de cada rodada.
 * É usado pelo GET /api/trilha e também pelos outros services (acesso a rodada, envio de respostas).
 */
@Service
public class TrilhaService {

    private final RodadaRepository rodadaRepository;
    private final ExercicioRepository exercicioRepository;
    private final ProgressoExercicioRepository progressoRepository;
    private final AlunoRepository alunoRepository;
    private final Clock clock;

    public TrilhaService(RodadaRepository rodadaRepository, ExercicioRepository exercicioRepository,
                         ProgressoExercicioRepository progressoRepository, AlunoRepository alunoRepository,
                         Clock clock) {
        this.rodadaRepository = rodadaRepository;
        this.exercicioRepository = exercicioRepository;
        this.progressoRepository = progressoRepository;
        this.alunoRepository = alunoRepository;
        this.clock = clock;
    }

    /** "Foto" da trilha para um usuário, calculada com poucas consultas fixas (sem N+1). */
    public record SituacaoTrilha(List<Rodada> rodadas,
                                 Map<Integer, List<Exercicio>> exerciciosPorRodada,
                                 Set<Integer> exerciciosConcluidos,
                                 Map<Integer, Integer> xpPorExercicio, // XP realmente ganho (PR7 pode ser metade)
                                 Map<Integer, StatusRodada> statusPorRodada,
                                 int faseMaisAltaLiberada) {

        public StatusRodada statusDa(Integer rodadaId) {
            return statusPorRodada.get(rodadaId);
        }

        public List<Exercicio> exerciciosDa(Integer rodadaId) {
            return exerciciosPorRodada.getOrDefault(rodadaId, List.of());
        }
    }

    @Transactional(readOnly = true)
    public SituacaoTrilha calcularSituacao(UsuarioLogado usuario) {
        List<Rodada> rodadas = rodadaRepository.findAllEmOrdemDaTrilha();              // 1 consulta (com o módulo)
        Map<Integer, List<Exercicio>> porRodada = exercicioRepository.findAll().stream() // 1 consulta
                .collect(Collectors.groupingBy(exercicio -> exercicio.getRodada().getId()));

        // exercicioId -> XP ganho, só dos exercícios CONCLUÍDOS (1 consulta)
        Map<Integer, Integer> xpPorExercicio = usuario.isAluno()
                ? progressoRepository.findByAlunoId(usuario.id()).stream()
                        .filter(ProgressoExercicio::isConcluido)
                        .collect(Collectors.toMap(progresso -> progresso.getExercicio().getId(),
                                ProgressoExercicio::getPontosGanhos))
                : Map.of();
        Set<Integer> concluidos = xpPorExercicio.keySet();

        List<StatusRodada> status;
        if (usuario.isAluno()) {
            List<Boolean> aprovadas = rodadas.stream()
                    .map(rodada -> RegrasTrilha.rodadaAprovada(
                            paraRegras(porRodada.getOrDefault(rodada.getId(), List.of())), concluidos))
                    .toList();
            status = RegrasTrilha.calcularStatus(aprovadas);
        } else {
            status = rodadas.stream().map(rodada -> StatusRodada.LIBERADA).toList(); // T3: professor/admin
        }

        Map<Integer, StatusRodada> statusPorRodada = new HashMap<>();
        for (int i = 0; i < rodadas.size(); i++) {
            statusPorRodada.put(rodadas.get(i).getId(), status.get(i));
        }
        List<Integer> ordemDasFases = rodadas.stream().map(rodada -> rodada.getModulo().getOrdenacao()).toList();

        return new SituacaoTrilha(rodadas, porRodada, concluidos, xpPorExercicio, statusPorRodada,
                RegrasTrilha.faseMaisAltaLiberada(ordemDasFases, status));
    }

    /** GET /api/trilha */
    @Transactional(readOnly = true)
    public TrilhaResponse montarTrilha(UsuarioLogado usuario) {
        SituacaoTrilha situacao = calcularSituacao(usuario);

        // agrupa as rodadas por fase, mantendo a ordem da trilha
        Map<Modulo, List<RodadaResumoResponse>> porFase = new LinkedHashMap<>();
        for (Rodada rodada : situacao.rodadas()) {
            porFase.computeIfAbsent(rodada.getModulo(), modulo -> new ArrayList<>())
                    .add(resumir(rodada, situacao));
        }
        List<FaseResponse> fases = porFase.entrySet().stream()
                .map(fase -> new FaseResponse(fase.getKey().getId(), fase.getKey().getOrdenacao(),
                        fase.getKey().getTitulo(), fase.getKey().getDescricao(), fase.getValue()))
                .toList();

        ResumoAlunoResponse resumoAluno = usuario.isAluno()
                ? ResumoAlunoResponse.de(alunoRepository.findById(usuario.id())
                        .orElseThrow(SessaoInvalidaException::new), LocalDate.now(clock))
                : null;

        return new TrilhaResponse(resumoAluno, fases);
    }

    private RodadaResumoResponse resumir(Rodada rodada, SituacaoTrilha situacao) {
        List<Exercicio> exercicios = situacao.exerciciosDa(rodada.getId());
        List<Exercicio> feitos = exercicios.stream()
                .filter(exercicio -> situacao.exerciciosConcluidos().contains(exercicio.getId()))
                .toList();
        return new RodadaResumoResponse(rodada.getId(), rodada.getOrdenacao(), rodada.getTitulo(),
                situacao.statusDa(rodada.getId()),
                feitos.size(), exercicios.size(),
                xpGanho(feitos, situacao), somaPontos(exercicios));
    }

    /** XP que o aluno realmente ganhou (gravado no progresso), e não os pontos "de tabela" (PR7). */
    private static int xpGanho(List<Exercicio> feitos, SituacaoTrilha situacao) {
        return feitos.stream()
                .mapToInt(exercicio -> situacao.xpPorExercicio().getOrDefault(exercicio.getId(), 0))
                .sum();
    }

    private static int somaPontos(List<Exercicio> exercicios) {
        return exercicios.stream().mapToInt(Exercicio::getPontos).sum();
    }

    static List<RegrasTrilha.ExercicioDaRodada> paraRegras(List<Exercicio> exercicios) {
        return exercicios.stream()
                .map(exercicio -> new RegrasTrilha.ExercicioDaRodada(exercicio.getId(),
                        exercicio.getTipoExercicio() == TipoExercicio.BOSS_CHALLENGE))
                .toList();
    }
}
