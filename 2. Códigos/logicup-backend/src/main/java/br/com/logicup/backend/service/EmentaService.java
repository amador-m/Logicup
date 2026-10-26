package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.EmentaResponse;
import br.com.logicup.backend.dto.EmentaResponse.FaseEmenta;
import br.com.logicup.backend.dto.EmentaResponse.QuestaoEmenta;
import br.com.logicup.backend.dto.EmentaResponse.RodadaEmenta;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.Modulo;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.ModuloRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Monta a ementa (Sprint 7): fase -> rodadas -> questões, com objetivos e habilidades.
 * Usa só 3 consultas fixas (módulos, rodadas, exercícios), qualquer que seja o tamanho da trilha:
 * os agrupamentos são feitos em memória, sem N+1.
 */
@Service
public class EmentaService {

    private static final Logger log = LoggerFactory.getLogger(EmentaService.class);

    private final ModuloRepository moduloRepository;
    private final RodadaRepository rodadaRepository;
    private final ExercicioRepository exercicioRepository;
    private final JsonMapper jsonMapper;

    public EmentaService(ModuloRepository moduloRepository, RodadaRepository rodadaRepository,
                         ExercicioRepository exercicioRepository, JsonMapper jsonMapper) {
        this.moduloRepository = moduloRepository;
        this.rodadaRepository = rodadaRepository;
        this.exercicioRepository = exercicioRepository;
        this.jsonMapper = jsonMapper;
    }

    /** Só o pedaço do conteudo_teorico que a ementa usa; "teoria" e "exemplos" são ignorados. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ObjetivosDoConteudo(List<String> objetivos) {
    }

    @Transactional(readOnly = true)
    public EmentaResponse montar() {
        List<Modulo> modulos = moduloRepository.findAllByOrderByOrdenacaoAsc();      // 1 consulta
        Map<Integer, List<Rodada>> rodadasPorModulo = rodadaRepository.findAllEmOrdemDaTrilha().stream() // 1 consulta
                .collect(Collectors.groupingBy(rodada -> rodada.getModulo().getId()));
        Map<Integer, List<Exercicio>> exerciciosPorRodada = exercicioRepository.findAll().stream()    // 1 consulta
                .collect(Collectors.groupingBy(exercicio -> exercicio.getRodada().getId()));

        List<FaseEmenta> fases = modulos.stream()
                .map(modulo -> new FaseEmenta(modulo.getId(), modulo.getOrdenacao(), modulo.getTitulo(),
                        modulo.getDescricao(),
                        rodadasPorModulo.getOrDefault(modulo.getId(), List.of()).stream()
                                .map(rodada -> paraRodada(rodada,
                                        exerciciosPorRodada.getOrDefault(rodada.getId(), List.of())))
                                .toList()))
                .toList();

        return new EmentaResponse(fases);
    }

    private RodadaEmenta paraRodada(Rodada rodada, List<Exercicio> exercicios) {
        List<QuestaoEmenta> questoes = exercicios.stream()
                .sorted(Comparator.comparing(Exercicio::getOrdenacao)) // findAll() não garante ordem
                .map(exercicio -> new QuestaoEmenta(exercicio.getId(), exercicio.getCodigoQuestao(),
                        exercicio.getOrdenacao(), exercicio.getTitulo(), exercicio.getTipoExercicio(),
                        exercicio.getPontos(), exercicio.getHabilidade()))
                .toList();
        return new RodadaEmenta(rodada.getId(), rodada.getOrdenacao(), rodada.getTitulo(),
                lerObjetivos(jsonMapper, rodada.getConteudoTeorico(), rodada.getId()), questoes);
    }

    /**
     * Lê a lista "objetivos" do JSON da rodada. Conteúdo sem objetivos, ou com JSON em formato
     * inesperado, vira lista VAZIA: um erro de conteúdo de UMA rodada não pode derrubar a ementa
     * inteira (500). O problema fica registrado no log para ser corrigido no banco.
     * É static e "package-private" para ser testado sem subir o Spring (EmentaServiceTest).
     */
    static List<String> lerObjetivos(JsonMapper jsonMapper, String conteudoJson, Integer rodadaId) {
        if (conteudoJson == null || conteudoJson.isBlank()) {
            return List.of();
        }
        try {
            ObjetivosDoConteudo conteudo = jsonMapper.readValue(conteudoJson, ObjetivosDoConteudo.class);
            if (conteudo == null || conteudo.objetivos() == null) {
                return List.of();
            }
            return conteudo.objetivos().stream().filter(Objects::nonNull).toList();
        } catch (RuntimeException e) { // no Jackson 3 as exceções de leitura são unchecked
            log.warn("conteudo_teorico da rodada {} com formato inesperado: {}", rodadaId, e.getMessage());
            return List.of();
        }
    }
}
