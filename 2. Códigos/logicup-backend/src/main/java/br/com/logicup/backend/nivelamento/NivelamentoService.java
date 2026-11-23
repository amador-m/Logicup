package br.com.logicup.backend.nivelamento;

import br.com.logicup.backend.dto.NivelamentoResponse;
import br.com.logicup.backend.dto.QuestaoNivelamentoResponse;
import br.com.logicup.backend.dto.RespostasNivelamentoRequest;
import br.com.logicup.backend.dto.ResultadoNivelamentoResponse;
import br.com.logicup.backend.dto.SituacaoNivelamento;
import br.com.logicup.backend.exception.NivelamentoJaRealizadoException;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Nivelamento;
import br.com.logicup.backend.model.QuestaoNivelamento;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.NivelamentoRepository;
import br.com.logicup.backend.repository.ProgressoExercicioRepository;
import br.com.logicup.backend.repository.QuestaoNivelamentoRepository;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.RegrasTrilha;
import br.com.logicup.backend.service.TrilhaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Sprint 11: QUIZ DE NIVELAMENTO (decisões da equipe):
 *  - oferecido só no PRIMEIRO acesso: o aluno ainda não tem registro de nivelamento e ainda não respondeu
 *    nada na trilha. Ele escolhe UMA vez: fazer o teste ou começar do zero;
 *  - 10 questões próprias (2 por fase, Fases 1 a 5), corrigidas AQUI no servidor: a resposta certa nunca
 *    vai para o navegador antes do envio;
 *  - fase dominada = acertou as duas questões dela. As fases dominadas EM SEQUÊNCIA desde a Fase 1 ficam
 *    liberadas (RegrasTrilha); liberar não dá XP, gemas nem conquistas.
 *
 * Quem aplica o resultado na trilha é o TrilhaService (lê o registro e libera as rodadas). Aqui só se
 * decide e grava.
 */
@Service
public class NivelamentoService {

    private final QuestaoNivelamentoRepository questaoRepository;
    private final NivelamentoRepository nivelamentoRepository;
    private final ProgressoExercicioRepository progressoRepository;
    private final AlunoRepository alunoRepository;
    private final TrilhaService trilhaService;
    private final Clock clock;

    public NivelamentoService(QuestaoNivelamentoRepository questaoRepository,
                              NivelamentoRepository nivelamentoRepository,
                              ProgressoExercicioRepository progressoRepository, AlunoRepository alunoRepository,
                              TrilhaService trilhaService, Clock clock) {
        this.questaoRepository = questaoRepository;
        this.nivelamentoRepository = nivelamentoRepository;
        this.progressoRepository = progressoRepository;
        this.alunoRepository = alunoRepository;
        this.trilhaService = trilhaService;
        this.clock = clock;
    }

    /** GET /api/nivelamento: a situação e, se ainda estiver pendente, as questões. */
    @Transactional(readOnly = true)
    public NivelamentoResponse situacao(Integer alunoId) {
        var registro = nivelamentoRepository.findById(alunoId);
        if (registro.isPresent()) {
            Nivelamento n = registro.get();
            return n.isFezTeste()
                    ? new NivelamentoResponse(SituacaoNivelamento.FEITO, null, n.getAcertos(), n.getTotal(),
                            n.getFasesLiberadas())
                    : new NivelamentoResponse(SituacaoNivelamento.NAO_QUIS, null, null, null, 0);
        }
        if (progressoRepository.existsByAlunoId(alunoId)) {
            return new NivelamentoResponse(SituacaoNivelamento.NAO_DISPONIVEL, null, null, null, 0);
        }
        List<QuestaoNivelamentoResponse> questoes = questaoRepository.findAllByOrderByFaseAscOrdenacaoAsc().stream()
                .map(QuestaoNivelamentoResponse::de)
                .toList();
        return new NivelamentoResponse(SituacaoNivelamento.PENDENTE, questoes, null, null, null);
    }

    /** POST /api/nivelamento/respostas: corrige, grava e devolve o resultado com a correção de cada questão. */
    @Transactional
    public ResultadoNivelamentoResponse responder(UsuarioLogado usuario, RespostasNivelamentoRequest dados) {
        Aluno aluno = alunoRepository.findById(usuario.id()).orElseThrow(SessaoInvalidaException::new);
        exigirPendente(usuario.id());

        List<QuestaoNivelamento> questoes = questaoRepository.findAllByOrderByFaseAscOrdenacaoAsc();
        Map<Integer, String> escolhidas = new HashMap<>();
        for (RespostasNivelamentoRequest.Resposta resposta : dados.respostas()) {
            if (escolhidas.put(resposta.questaoId(), resposta.alternativa()) != null) {
                throw new RequisicaoInvalidaException("Cada questão deve ser respondida uma vez só.");
            }
        }
        Set<Integer> idsDoTeste = questoes.stream().map(QuestaoNivelamento::getId).collect(Collectors.toSet());
        if (!escolhidas.keySet().equals(idsDoTeste)) {
            throw new RequisicaoInvalidaException("Responda todas as questões do nivelamento.");
        }

        // corrige: fase -> [acertou a 1ª?, acertou a 2ª?] (TreeMap: fases em ordem)
        Map<Integer, List<Boolean>> acertouPorFase = new TreeMap<>();
        List<ResultadoNivelamentoResponse.Correcao> correcoes = new ArrayList<>();
        int acertos = 0;
        for (QuestaoNivelamento questao : questoes) {
            boolean acertou = questao.getCorreta().equalsIgnoreCase(escolhidas.get(questao.getId()).trim());
            if (acertou) {
                acertos++;
            }
            acertouPorFase.computeIfAbsent(questao.getFase(), fase -> new ArrayList<>()).add(acertou);
            correcoes.add(new ResultadoNivelamentoResponse.Correcao(questao.getId(), questao.getFase(), acertou,
                    questao.getCorreta(), questao.getExplicacao()));
        }
        int fasesLiberadas = RegrasTrilha.fasesDominadasEmSequencia(acertouPorFase);

        nivelamentoRepository.save(Nivelamento.comTeste(aluno.getId(), acertos, questoes.size(), fasesLiberadas,
                LocalDateTime.now(clock)));

        // o nível acompanha a trilha (N-A: fase mais alta liberada), como depois de enviar uma rodada
        aluno.atualizarNivel(trilhaService.calcularSituacao(usuario).faseMaisAltaLiberada());

        return new ResultadoNivelamentoResponse(acertos, questoes.size(), fasesLiberadas, aluno.getNivelAtual(),
                correcoes);
    }

    /** POST /api/nivelamento/recusa: "prefiro começar do zero". Grava a escolha para não perguntar de novo. */
    @Transactional
    public void recusar(Integer alunoId) {
        alunoRepository.findById(alunoId).orElseThrow(SessaoInvalidaException::new);
        exigirPendente(alunoId);
        nivelamentoRepository.save(Nivelamento.semTeste(alunoId, LocalDateTime.now(clock)));
    }

    private void exigirPendente(Integer alunoId) {
        if (nivelamentoRepository.existsById(alunoId) || progressoRepository.existsByAlunoId(alunoId)) {
            throw new NivelamentoJaRealizadoException();
        }
    }
}
