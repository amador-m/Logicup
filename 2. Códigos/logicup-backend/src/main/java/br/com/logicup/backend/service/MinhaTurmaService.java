package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.EntrarTurmaRequest;
import br.com.logicup.backend.dto.MinhaTurmaResponse;
import br.com.logicup.backend.dto.RankingTurmaResponse;
import br.com.logicup.backend.dto.StatusTarefa;
import br.com.logicup.backend.dto.TarefaDoAlunoResponse;
import br.com.logicup.backend.exception.AlunoJaEstaNaTurmaException;
import br.com.logicup.backend.exception.AlunoRemovidoDaTurmaException;
import br.com.logicup.backend.exception.RecursoNaoEncontradoException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.AlunoTurma;
import br.com.logicup.backend.model.AlunoTurmaId;
import br.com.logicup.backend.model.StatusMatricula;
import br.com.logicup.backend.model.Turma;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.AlunoTurmaRepository;
import br.com.logicup.backend.repository.TarefaRepository;
import br.com.logicup.backend.repository.TurmaRepository;
import br.com.logicup.backend.security.ProtecaoForcaBruta;
import br.com.logicup.backend.service.TrilhaService.SituacaoTrilha;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Sprint 9: regras das turmas do lado do ALUNO ("Minhas turmas").
 * Sprint 11: o aluno pode estar em VÁRIAS turmas ao mesmo tempo (o banco já permitia: a chave de aluno_turma é
 * aluno + turma). Quando a URL traz o id de uma turma, a regra confere se o aluno do TOKEN tem matrícula ATIVA
 * nela: o id na URL só escolhe entre as turmas DELE, nunca dá acesso à turma de outra pessoa.
 */
@Service
public class MinhaTurmaService {

    private final TurmaRepository turmaRepository;
    private final AlunoTurmaRepository alunoTurmaRepository;
    private final AlunoRepository alunoRepository;
    private final TarefaRepository tarefaRepository;
    private final ProgressoNaTurma progresso;
    private final ConquistaService conquistaService;
    private final Clock clock;
    private final ProtecaoForcaBruta protecao; // Sprint 9 (D2): limite de códigos errados

    public MinhaTurmaService(TurmaRepository turmaRepository, AlunoTurmaRepository alunoTurmaRepository,
                             AlunoRepository alunoRepository, TarefaRepository tarefaRepository,
                             ProgressoNaTurma progresso, ConquistaService conquistaService, Clock clock,
                             ProtecaoForcaBruta protecao) {
        this.turmaRepository = turmaRepository;
        this.alunoTurmaRepository = alunoTurmaRepository;
        this.alunoRepository = alunoRepository;
        this.tarefaRepository = tarefaRepository;
        this.progresso = progresso;
        this.conquistaService = conquistaService;
        this.clock = clock;
        this.protecao = protecao;
    }

    /**
     * Entrar com o código (já normalizado no EntrarTurmaRequest — R4). A ordem das checagens importa:
     *  0) passou do limite de códigos errados?  sim -> 429 (Sprint 9, D2)
     *  1) código existe?                        não -> 404 (e conta 1 erro no limite)
     *  2) já passou por ESTA turma antes?       ATIVO -> 409 (Sprint 11: já está nela);
     *                                           REMOVIDO -> 403 (R7); SAIU -> a mesma linha volta a ATIVO (R6)
     *  3) senão, matrícula nova. Sprint 11: estar em OUTRA turma não impede mais (antes era 409, R5).
     */
    @Transactional
    public MinhaTurmaResponse entrar(Integer alunoId, EntrarTurmaRequest dados) {
        // Sprint 9 (D2): 10 códigos errados em 15 min -> 429. Sprint 10: confere e conta de uma vez (atômico)
        protecao.reservarTentativaDeCodigoTurma(alunoId);
        Optional<Turma> encontrada = turmaRepository.findByCodigoConvite(dados.codigo());
        if (encontrada.isEmpty()) {
            throw new RecursoNaoEncontradoException("Código de turma não encontrado. Confira com o professor.");
        }
        protecao.registrarCodigoTurmaEncontrado(alunoId); // só código INEXISTENTE conta (409/403 não)
        Turma turma = encontrada.get();
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow(SessaoInvalidaException::new);

        Optional<AlunoTurma> anterior = alunoTurmaRepository.findById(new AlunoTurmaId(alunoId, turma.getId()));
        if (anterior.isPresent()) {
            AlunoTurma matricula = anterior.get();
            switch (matricula.getStatus()) {
                case ATIVO -> throw new AlunoJaEstaNaTurmaException();
                case REMOVIDO -> throw new AlunoRemovidoDaTurmaException();
                case SAIU -> matricula.reativar(LocalDateTime.now(clock));
            }
        } else {
            alunoTurmaRepository.save(new AlunoTurma(aluno, turma));
        }
        conquistaService.avaliar(aluno); // Sprint 9 (B1): conquista "Colega de turma"
        return montar(turma, aluno);
    }

    /** Sprint 11: todas as turmas ATIVAS do aluno (lista vazia = nenhuma; a tela mostra o campo do código). */
    @Transactional(readOnly = true)
    public List<MinhaTurmaResponse> listar(Integer alunoId) {
        return alunoTurmaRepository.findByAlunoIdAndStatusOrderByEntrouEmAscTurmaIdAsc(alunoId, StatusMatricula.ATIVO)
                .stream()
                .map(matricula -> montar(matricula.getTurma(), matricula.getAluno()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RankingTurmaResponse ranking(Integer alunoId, Integer turmaId) {
        Turma turma = matriculaAtiva(alunoId, turmaId).getTurma();
        List<Aluno> membros = alunoTurmaRepository.findByTurmaIdAndStatus(turma.getId(), StatusMatricula.ATIVO)
                .stream().map(AlunoTurma::getAluno).toList();
        return ProgressoNaTurma.ranking(membros, alunoId);
    }

    /** R6: sair muda para SAIU (a linha fica; se voltar com o código, ela é reaproveitada). As outras turmas não mudam. */
    @Transactional
    public void sair(Integer alunoId, Integer turmaId) {
        matriculaAtiva(alunoId, turmaId).sair(LocalDateTime.now(clock));
    }

    // ---------------------------------------------------------------- apoio

    /**
     * A matrícula ATIVA deste aluno NESTA turma. Não estar na turma (ou a turma nem existir) dá o mesmo 404:
     * assim a resposta não revela se existe uma turma com aquele id.
     */
    private AlunoTurma matriculaAtiva(Integer alunoId, Integer turmaId) {
        return alunoTurmaRepository.findById(new AlunoTurmaId(alunoId, turmaId))
                .filter(AlunoTurma::isAtiva)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Você não está nesta turma."));
    }

    private MinhaTurmaResponse montar(Turma turma, Aluno aluno) {
        SituacaoTrilha situacao = progresso.situacaoDe(aluno);
        LocalDate hoje = LocalDate.now(clock);
        List<TarefaDoAlunoResponse> tarefas = tarefaRepository.findByTurmaIdOrderByIdAsc(turma.getId()).stream()
                .map(tarefa -> {
                    StatusTarefa status = ProgressoNaTurma.statusDa(tarefa, situacao);
                    return new TarefaDoAlunoResponse(tarefa.getId(), tarefa.getRodada().getId(),
                            tarefa.getRodada().getTitulo(), tarefa.getRodada().getModulo().getTitulo(),
                            tarefa.getPrazo(), status, ProgressoNaTurma.atrasada(tarefa, status, hoje));
                })
                .toList();
        String professor = turma.getProfessor() != null ? turma.getProfessor().getNome() : null;
        return new MinhaTurmaResponse(turma.getId(), turma.getNomeTurma(), professor, tarefas);
    }
}
