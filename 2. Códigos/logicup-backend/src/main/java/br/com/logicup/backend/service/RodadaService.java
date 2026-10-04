package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.ExercicioResponse;
import br.com.logicup.backend.dto.RodadaDetalheResponse;
import br.com.logicup.backend.exception.RecursoNaoEncontradoException;
import br.com.logicup.backend.exception.RodadaBloqueadaException;
import br.com.logicup.backend.model.Dica;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.ProgressoExercicio;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.StatusRodada;
import br.com.logicup.backend.repository.DicaRepository;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ProgressoExercicioRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import br.com.logicup.backend.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** GET /api/rodadas/{id}: conteúdo da rodada e seus exercícios, SEM gabarito. */
@Service
public class RodadaService {

    private final RodadaRepository rodadaRepository;
    private final ExercicioRepository exercicioRepository;
    private final DicaRepository dicaRepository;
    private final ProgressoExercicioRepository progressoRepository;
    private final TrilhaService trilhaService;

    public RodadaService(RodadaRepository rodadaRepository, ExercicioRepository exercicioRepository,
                         DicaRepository dicaRepository, ProgressoExercicioRepository progressoRepository,
                         TrilhaService trilhaService) {
        this.rodadaRepository = rodadaRepository;
        this.exercicioRepository = exercicioRepository;
        this.dicaRepository = dicaRepository;
        this.progressoRepository = progressoRepository;
        this.trilhaService = trilhaService;
    }

    @Transactional(readOnly = true)
    public RodadaDetalheResponse detalhar(Integer rodadaId, UsuarioLogado usuario) {
        Rodada rodada = buscarRodada(rodadaId);
        StatusRodada status = verificarAcesso(rodadaId, usuario);

        List<Exercicio> exercicios = exercicioRepository.findByRodadaIdOrderByOrdenacaoAsc(rodadaId);
        Map<Integer, List<String>> dicasPorExercicio = dicaRepository.findByExercicioRodadaIdOrderByIdAsc(rodadaId)
                .stream()
                .collect(Collectors.groupingBy(dica -> dica.getExercicio().getId(),
                        Collectors.mapping(Dica::getTextoDica, Collectors.toList())));
        Map<Integer, ProgressoExercicio> progressoPorExercicio = usuario.isAluno()
                ? progressoRepository.findByAlunoIdAndExercicioRodadaId(usuario.id(), rodadaId).stream()
                        .collect(Collectors.toMap(progresso -> progresso.getExercicio().getId(), Function.identity()))
                : Map.of();

        List<ExercicioResponse> exerciciosResponse = exercicios.stream()
                .map(exercicio -> paraResponse(exercicio,
                        dicasPorExercicio.getOrDefault(exercicio.getId(), List.of()),
                        progressoPorExercicio.get(exercicio.getId())))
                .toList();

        return new RodadaDetalheResponse(rodada.getId(), rodada.getOrdenacao(), rodada.getTitulo(),
                rodada.getModulo().getOrdenacao(), rodada.getModulo().getTitulo(), status,
                rodada.getConteudoTeorico(), exerciciosResponse);
    }

    Rodada buscarRodada(Integer rodadaId) {
        return rodadaRepository.findById(rodadaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Rodada não encontrada."));
    }

    /** Aluno só acessa rodada liberada/aprovada; professor e admin acessam tudo (T3). */
    StatusRodada verificarAcesso(Integer rodadaId, UsuarioLogado usuario) {
        StatusRodada status = trilhaService.calcularSituacao(usuario).statusDa(rodadaId);
        if (status == StatusRodada.BLOQUEADA) {
            throw new RodadaBloqueadaException();
        }
        return status;
    }

    private static ExercicioResponse paraResponse(Exercicio exercicio, List<String> dicas, ProgressoExercicio progresso) {
        return new ExercicioResponse(exercicio.getId(), exercicio.getCodigoQuestao(), exercicio.getTitulo(),
                exercicio.getOrdenacao(), exercicio.getTipoExercicio(), exercicio.getFormatoResposta(),
                exercicio.getPontos(), exercicio.getEnunciado(), exercicio.getCodigoApoio(),
                exercicio.getAlternativasJson(), dicas,
                progresso != null && progresso.isConcluido(),
                progresso != null ? progresso.getTentativas() : 0);
        // repare: getGabaritoJson() NUNCA é chamado aqui
    }
}
