package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.AlunoDaTurmaResponse;
import br.com.logicup.backend.dto.RankingTurmaResponse;
import br.com.logicup.backend.dto.StatusTarefa;
import br.com.logicup.backend.dto.TarefaDaTurmaResponse;
import br.com.logicup.backend.dto.TarefaRequest;
import br.com.logicup.backend.dto.TurmaDetalheResponse;
import br.com.logicup.backend.dto.TurmaRequest;
import br.com.logicup.backend.dto.TurmaResumoResponse;
import br.com.logicup.backend.exception.RecursoNaoEncontradoException;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.exception.TarefaJaAtribuidaException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.AlunoTurma;
import br.com.logicup.backend.model.AlunoTurmaId;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.StatusMatricula;
import br.com.logicup.backend.model.Tarefa;
import br.com.logicup.backend.model.Turma;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AlunoTurmaRepository;
import br.com.logicup.backend.repository.RodadaRepository;
import br.com.logicup.backend.repository.TarefaRepository;
import br.com.logicup.backend.repository.TurmaRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.service.TrilhaService.SituacaoTrilha;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sprint 9: regras das turmas do lado do PROFESSOR.
 *
 * Regra de ouro (R8): toda operação começa por turmaDoProfessor(), que busca a turma pelo id E pelo
 * professor do token. Turma de outro professor e turma inexistente caem no MESMO 404 — o sistema não
 * confirma nem que a turma existe. Nenhum método recebe o id do professor pelo corpo da requisição.
 */
@Service
public class TurmaService {

    private static final int TENTATIVAS_CODIGO = 10;
    private static final String TURMA_NAO_ENCONTRADA = "Turma não encontrada.";

    private final TurmaRepository turmaRepository;
    private final AlunoTurmaRepository alunoTurmaRepository;
    private final TarefaRepository tarefaRepository;
    private final RodadaRepository rodadaRepository;
    private final UsuarioRepository usuarioRepository;
    private final GeradorCodigoConvite geradorCodigo;
    private final ProgressoNaTurma progresso;
    private final Clock clock;

    public TurmaService(TurmaRepository turmaRepository, AlunoTurmaRepository alunoTurmaRepository,
                        TarefaRepository tarefaRepository, RodadaRepository rodadaRepository,
                        UsuarioRepository usuarioRepository, GeradorCodigoConvite geradorCodigo,
                        ProgressoNaTurma progresso, Clock clock) {
        this.turmaRepository = turmaRepository;
        this.alunoTurmaRepository = alunoTurmaRepository;
        this.tarefaRepository = tarefaRepository;
        this.rodadaRepository = rodadaRepository;
        this.usuarioRepository = usuarioRepository;
        this.geradorCodigo = geradorCodigo;
        this.progresso = progresso;
        this.clock = clock;
    }

    // ---------------------------------------------------------------- turma

    @Transactional
    public TurmaResumoResponse criar(Integer professorId, TurmaRequest dados) {
        Usuario professor = usuarioRepository.findById(professorId).orElseThrow(SessaoInvalidaException::new);
        Turma turma = turmaRepository.save(new Turma(dados.nome(), professor, codigoUnico()));
        return TurmaResumoResponse.de(turma, 0);
    }

    @Transactional(readOnly = true)
    public List<TurmaResumoResponse> listar(Integer professorId) {
        return turmaRepository.findByProfessorIdOrderByIdDesc(professorId).stream()
                .map(turma -> TurmaResumoResponse.de(turma, ativos(turma)))
                .toList();
    }

    @Transactional
    public TurmaResumoResponse renomear(Integer professorId, Integer turmaId, TurmaRequest dados) {
        Turma turma = turmaDoProfessor(professorId, turmaId);
        turma.setNomeTurma(dados.nome()); // entidade gerenciada: o Hibernate grava no fim da transação
        return TurmaResumoResponse.de(turma, ativos(turma));
    }

    /** R3: o código antigo deixa de funcionar na hora (é substituído na mesma coluna). */
    @Transactional
    public TurmaResumoResponse gerarNovoCodigo(Integer professorId, Integer turmaId) {
        Turma turma = turmaDoProfessor(professorId, turmaId);
        turma.setCodigoConvite(codigoUnico());
        return TurmaResumoResponse.de(turma, ativos(turma));
    }

    /**
     * R14: apaga tarefas e matrículas da turma e depois a turma. O progresso dos alunos não é tocado.
     * Apaga entidade por entidade (e não com um DELETE em massa): assim o Hibernate sabe o que saiu
     * e não tenta gravar depois uma matrícula que aponta para uma turma que já não existe.
     */
    @Transactional
    public void excluir(Integer professorId, Integer turmaId) {
        Turma turma = turmaDoProfessor(professorId, turmaId);
        tarefaRepository.deleteAll(tarefaRepository.findByTurmaIdOrderByIdAsc(turma.getId()));
        alunoTurmaRepository.deleteAll(alunoTurmaRepository.findByTurmaId(turma.getId()));
        turmaRepository.delete(turma);
    }

    // ---------------------------------------------------------------- acompanhamento (R9)

    @Transactional(readOnly = true)
    public TurmaDetalheResponse detalhar(Integer professorId, Integer turmaId) {
        Turma turma = turmaDoProfessor(professorId, turmaId);
        List<Aluno> membros = membrosAtivos(turma);

        // a situação de cada aluno é calculada UMA vez e usada para a linha dele e para as tarefas
        Map<Aluno, SituacaoTrilha> situacoes = new LinkedHashMap<>();
        membros.stream()
                .sorted(Comparator.comparing(Aluno::getNome, String.CASE_INSENSITIVE_ORDER))
                .forEach(aluno -> situacoes.put(aluno, progresso.situacaoDe(aluno)));

        List<AlunoDaTurmaResponse> alunos = situacoes.entrySet().stream()
                .map(item -> {
                    Aluno aluno = item.getKey();
                    return new AlunoDaTurmaResponse(aluno.getId(), aluno.getNome(), aluno.getUsername(),
                            aluno.getEmail(), aluno.getPontuacaoAcumulada(), aluno.getNivelAtual(),
                            ProgressoNaTurma.rodadasAprovadas(item.getValue()), aluno.getUltimaAtividadeData(),
                            aluno.getAvatar());
                })
                .toList();

        List<TarefaDaTurmaResponse> tarefas = tarefaRepository.findByTurmaIdOrderByIdAsc(turma.getId()).stream()
                .map(tarefa -> paraProfessor(tarefa, situacoes.values().stream().toList()))
                .toList();

        return new TurmaDetalheResponse(turma.getId(), turma.getNomeTurma(), turma.getCodigoConvite(),
                alunos.size(), alunos, tarefas);
    }

    /** R7: só remove quem está ATIVO nesta turma; o aluno passa a REMOVIDO (a linha fica como histórico). */
    @Transactional
    public void removerAluno(Integer professorId, Integer turmaId, Integer alunoId) {
        Turma turma = turmaDoProfessor(professorId, turmaId);
        AlunoTurma matricula = alunoTurmaRepository.findById(new AlunoTurmaId(alunoId, turma.getId()))
                .filter(AlunoTurma::isAtiva)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Este aluno não está na turma."));
        matricula.remover(LocalDateTime.now(clock));
    }

    @Transactional(readOnly = true)
    public RankingTurmaResponse ranking(Integer professorId, Integer turmaId) {
        Turma turma = turmaDoProfessor(professorId, turmaId);
        return ProgressoNaTurma.ranking(membrosAtivos(turma), null);
    }

    // ---------------------------------------------------------------- tarefas (R11 a R13)

    @Transactional
    public TarefaDaTurmaResponse criarTarefa(Integer professorId, Integer turmaId, TarefaRequest dados) {
        Turma turma = turmaDoProfessor(professorId, turmaId);
        Rodada rodada = rodadaRepository.findById(dados.rodadaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Rodada não encontrada."));
        if (dados.prazo() != null && dados.prazo().isBefore(LocalDate.now(clock))) {
            throw new RequisicaoInvalidaException("O prazo não pode estar no passado.");      // R12
        }
        if (tarefaRepository.existsByTurmaIdAndRodadaId(turma.getId(), rodada.getId())) {
            throw new TarefaJaAtribuidaException();                                            // R11
        }
        Tarefa tarefa = tarefaRepository.save(new Tarefa(turma, rodada, dados.prazo()));
        List<SituacaoTrilha> situacoes = membrosAtivos(turma).stream().map(progresso::situacaoDe).toList();
        return paraProfessor(tarefa, situacoes);
    }

    /** R13: apaga só a tarefa; o progresso é da rodada e continua intacto. */
    @Transactional
    public void removerTarefa(Integer professorId, Integer turmaId, Integer tarefaId) {
        Turma turma = turmaDoProfessor(professorId, turmaId);
        Tarefa tarefa = tarefaRepository.findByIdAndTurmaId(tarefaId, turma.getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tarefa não encontrada."));
        tarefaRepository.delete(tarefa);
    }

    // ---------------------------------------------------------------- apoio

    /** R8: a ÚNICA porta de entrada para uma turma no lado do professor. */
    private Turma turmaDoProfessor(Integer professorId, Integer turmaId) {
        return turmaRepository.findByIdAndProfessorId(turmaId, professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(TURMA_NAO_ENCONTRADA));
    }

    private List<Aluno> membrosAtivos(Turma turma) {
        return alunoTurmaRepository.findByTurmaIdAndStatus(turma.getId(), StatusMatricula.ATIVO).stream()
                .map(AlunoTurma::getAluno)
                .toList();
    }

    private long ativos(Turma turma) {
        return alunoTurmaRepository.countByTurmaIdAndStatus(turma.getId(), StatusMatricula.ATIVO);
    }

    private static TarefaDaTurmaResponse paraProfessor(Tarefa tarefa, List<SituacaoTrilha> situacoes) {
        int concluiram = (int) situacoes.stream()
                .filter(situacao -> ProgressoNaTurma.statusDa(tarefa, situacao) == StatusTarefa.CONCLUIDA)
                .count();
        Rodada rodada = tarefa.getRodada();
        return new TarefaDaTurmaResponse(tarefa.getId(), rodada.getId(), rodada.getTitulo(),
                rodada.getModulo().getTitulo(), tarefa.getPrazo(), concluiram);
    }

    /**
     * R3: sorteia até achar um código livre. Com ~852 bilhões de combinações, repetir é raríssimo;
     * o limite de tentativas só existe para nunca entrar em laço infinito.
     */
    private String codigoUnico() {
        for (int i = 0; i < TENTATIVAS_CODIGO; i++) {
            String codigo = geradorCodigo.gerar();
            if (!turmaRepository.existsByCodigoConvite(codigo)) {
                return codigo;
            }
        }
        throw new IllegalStateException("Não foi possível gerar um código de convite livre.");
    }
}
