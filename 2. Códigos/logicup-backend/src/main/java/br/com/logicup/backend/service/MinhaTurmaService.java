package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.EntrarTurmaRequest;
import br.com.logicup.backend.dto.MinhaTurmaResponse;
import br.com.logicup.backend.dto.RankingTurmaResponse;
import br.com.logicup.backend.dto.StatusTarefa;
import br.com.logicup.backend.dto.TarefaDoAlunoResponse;
import br.com.logicup.backend.exception.AlunoJaTemTurmaException;
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
 * Sprint 9: regras das turmas do lado do ALUNO ("Minha turma").
 * O aluno nunca informa QUAL turma é a dele: a matrícula ATIVA é descoberta pelo id do token.
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
     *  2) o aluno já tem uma turma ATIVA?       sim -> 409 (R5)
     *  3) já passou por ESTA turma antes?       REMOVIDO -> 403 (R7); SAIU -> a mesma linha volta a ATIVO (R6)
     *  4) senão, matrícula nova.
     */
    @Transactional
    public MinhaTurmaResponse entrar(Integer alunoId, EntrarTurmaRequest dados) {
        protecao.verificarCodigoTurma(alunoId); // Sprint 9 (D2): 10 códigos errados em 15 min -> 429
        Optional<Turma> encontrada = turmaRepository.findByCodigoConvite(dados.codigo());
        if (encontrada.isEmpty()) {
            protecao.registrarCodigoTurmaInexistente(alunoId); // só código INEXISTENTE conta (409/403 não)
            throw new RecursoNaoEncontradoException("Código de turma não encontrado. Confira com o professor.");
        }
        Turma turma = encontrada.get();
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow(SessaoInvalidaException::new);

        if (matriculaAtiva(alunoId).isPresent()) {
            throw new AlunoJaTemTurmaException();
        }

        Optional<AlunoTurma> anterior = alunoTurmaRepository.findById(new AlunoTurmaId(alunoId, turma.getId()));
        if (anterior.isPresent()) {
            AlunoTurma matricula = anterior.get();
            if (matricula.getStatus() == StatusMatricula.REMOVIDO) {
                throw new AlunoRemovidoDaTurmaException();
            }
            matricula.reativar(LocalDateTime.now(clock));
        } else {
            alunoTurmaRepository.save(new AlunoTurma(aluno, turma));
        }
        conquistaService.avaliar(aluno); // Sprint 9 (B1): conquista "Colega de turma"
        return montar(turma, aluno);
    }

    /** Vazio = o aluno não está em nenhuma turma (o controller responde 204). */
    @Transactional(readOnly = true)
    public Optional<MinhaTurmaResponse> buscar(Integer alunoId) {
        return matriculaAtiva(alunoId).map(matricula -> montar(matricula.getTurma(), matricula.getAluno()));
    }

    @Transactional(readOnly = true)
    public RankingTurmaResponse ranking(Integer alunoId) {
        Turma turma = matriculaObrigatoria(alunoId).getTurma();
        List<Aluno> membros = alunoTurmaRepository.findByTurmaIdAndStatus(turma.getId(), StatusMatricula.ATIVO)
                .stream().map(AlunoTurma::getAluno).toList();
        return ProgressoNaTurma.ranking(membros, alunoId);
    }

    /** R6: sair muda para SAIU (a linha fica; se voltar com o código, ela é reaproveitada). */
    @Transactional
    public void sair(Integer alunoId) {
        matriculaObrigatoria(alunoId).sair(LocalDateTime.now(clock));
    }

    // ---------------------------------------------------------------- apoio

    private Optional<AlunoTurma> matriculaAtiva(Integer alunoId) {
        return alunoTurmaRepository.findFirstByAlunoIdAndStatus(alunoId, StatusMatricula.ATIVO);
    }

    private AlunoTurma matriculaObrigatoria(Integer alunoId) {
        return matriculaAtiva(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Você não está em nenhuma turma."));
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
