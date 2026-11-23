package br.com.logicup.backend.admin;

import br.com.logicup.backend.dto.ConteudoAdminResponse;
import br.com.logicup.backend.dto.EditarExercicioRequest;
import br.com.logicup.backend.dto.EditarRodadaRequest;
import br.com.logicup.backend.dto.RodadaAdminResponse;
import br.com.logicup.backend.exception.RecursoNaoEncontradoException;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.Modulo;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.repository.ExercicioRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sprint 11: GESTÃO DE CONTEÚDOS pelo administrador (RF24: "editar conteúdos teóricos/expositivos").
 * Decisão da equipe: o admin edita os TEXTOS (título e teoria da rodada; título, enunciado e explicação dos
 * exercícios). Gabaritos, alternativas, tipos e a estrutura da trilha NÃO mudam por aqui: um gabarito errado
 * quebraria a correção automática, e a estrutura nova exigiria telas novas.
 *
 * ATENÇÃO (para a equipe): a fonte do conteúdo era o documento mestre (BancoDeQuestoes .docx -> seed).
 * Depois de editar pelo admin, o BANCO passa a ter a versão mais nova: se o seed for gerado de novo a partir
 * do documento, as edições se perdem. Toda edição fica registrada no log (quem, o quê).
 */
@Service
public class AdminConteudoService {

    private static final Logger log = LoggerFactory.getLogger(AdminConteudoService.class);

    private final RodadaRepository rodadaRepository;
    private final ExercicioRepository exercicioRepository;
    private final JsonMapper jsonMapper;

    public AdminConteudoService(RodadaRepository rodadaRepository, ExercicioRepository exercicioRepository,
                                JsonMapper jsonMapper) {
        this.rodadaRepository = rodadaRepository;
        this.exercicioRepository = exercicioRepository;
        this.jsonMapper = jsonMapper;
    }

    /** A árvore fase -> rodada -> exercício (só ids e títulos), com 2 consultas fixas. */
    @Transactional(readOnly = true)
    public ConteudoAdminResponse arvore() {
        Map<Integer, List<Exercicio>> porRodada = exercicioRepository.findAll().stream()
                .collect(Collectors.groupingBy(e -> e.getRodada().getId()));
        Map<Modulo, List<ConteudoAdminResponse.Rodada>> porFase = new LinkedHashMap<>();
        for (Rodada rodada : rodadaRepository.findAllEmOrdemDaTrilha()) {
            List<ConteudoAdminResponse.Exercicio> exercicios = porRodada.getOrDefault(rodada.getId(), List.of())
                    .stream()
                    .sorted((a, b) -> a.getOrdenacao().compareTo(b.getOrdenacao()))
                    .map(e -> new ConteudoAdminResponse.Exercicio(e.getId(), e.getCodigoQuestao(), e.getTitulo()))
                    .toList();
            porFase.computeIfAbsent(rodada.getModulo(), m -> new ArrayList<>())
                    .add(new ConteudoAdminResponse.Rodada(rodada.getId(), rodada.getOrdenacao(), rodada.getTitulo(),
                            exercicios));
        }
        return new ConteudoAdminResponse(porFase.entrySet().stream()
                .map(f -> new ConteudoAdminResponse.Fase(f.getKey().getId(), f.getKey().getOrdenacao(),
                        f.getKey().getTitulo(), f.getValue()))
                .toList());
    }

    @Transactional(readOnly = true)
    public RodadaAdminResponse rodada(Integer id) {
        return paraResposta(buscarRodada(id));
    }

    @Transactional
    public RodadaAdminResponse editarRodada(Integer adminId, Integer id, EditarRodadaRequest dados) {
        Rodada rodada = buscarRodada(id);
        JsonNode atual = jsonMapper.readTree(rodada.getConteudoTeorico());
        JsonNode novo = dados.conteudo().deepCopy(); // não mexe no objeto recebido

        RegrasEdicaoConteudo.exigirMesmaForma(atual, novo, "conteudo");
        RegrasEdicaoConteudo.exigirTextosPreenchidos(novo, "conteudo");
        RegrasEdicaoConteudo.recalcularTextoDasSecoes(novo);

        rodada.setTitulo(dados.titulo().trim());
        rodada.setConteudoTeorico(jsonMapper.writeValueAsString(novo));
        log.info("Admin {} editou os textos da rodada {}", adminId, id);
        return paraResposta(rodada);
    }

    @Transactional
    public RodadaAdminResponse.Exercicio editarExercicio(Integer adminId, Integer id, EditarExercicioRequest dados) {
        Exercicio exercicio = exercicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Exercício não encontrado."));
        exercicio.setTitulo(dados.titulo().trim());
        exercicio.setEnunciado(dados.enunciado().trim());
        exercicio.setExplicacao(dados.explicacao().trim());
        log.info("Admin {} editou os textos do exercício {} ({})", adminId, id, exercicio.getCodigoQuestao());
        return paraResposta(exercicio);
    }

    private Rodada buscarRodada(Integer id) {
        Rodada rodada = rodadaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Rodada não encontrada."));
        if (rodada.getConteudoTeorico() == null) {
            throw new RequisicaoInvalidaException("Esta rodada não tem teoria cadastrada.");
        }
        return rodada;
    }

    private RodadaAdminResponse paraResposta(Rodada rodada) {
        List<RodadaAdminResponse.Exercicio> exercicios = exercicioRepository
                .findByRodadaIdOrderByOrdenacaoAsc(rodada.getId()).stream()
                .map(AdminConteudoService::paraResposta)
                .toList();
        return new RodadaAdminResponse(rodada.getId(), rodada.getModulo().getOrdenacao(),
                rodada.getModulo().getTitulo(), rodada.getOrdenacao(), rodada.getTitulo(),
                rodada.getConteudoTeorico(), exercicios);
    }

    private static RodadaAdminResponse.Exercicio paraResposta(Exercicio e) {
        return new RodadaAdminResponse.Exercicio(e.getId(), e.getCodigoQuestao(), e.getTitulo(), e.getTipoExercicio(),
                e.getEnunciado(), e.getExplicacao());
    }
}
