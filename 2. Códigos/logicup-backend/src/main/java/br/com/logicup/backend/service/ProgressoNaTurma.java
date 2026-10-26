package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.PosicaoRankingResponse;
import br.com.logicup.backend.dto.RankingTurmaResponse;
import br.com.logicup.backend.dto.StatusTarefa;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.StatusRodada;
import br.com.logicup.backend.model.Tarefa;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.TrilhaService.SituacaoTrilha;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Sprint 9: o que o lado do professor (TurmaService) e o do aluno (MinhaTurmaService) calculam IGUAL:
 * progresso de um aluno, status de uma tarefa e ranking da turma.
 *
 * R9: nada aqui recalcula a trilha "por conta própria". O progresso de cada aluno vem do MESMO
 * TrilhaService.calcularSituacao usado na tela da trilha — como se fosse o próprio aluno pedindo.
 * Assim professor, aluno e trilha nunca discordam sobre o que está aprovado ou bloqueado.
 */
@Component
public class ProgressoNaTurma {

    private final TrilhaService trilhaService;

    public ProgressoNaTurma(TrilhaService trilhaService) {
        this.trilhaService = trilhaService;
    }

    /**
     * A trilha vista por ESTE aluno. Custo: 3 consultas por aluno (rodadas, exercícios, progresso).
     * Para turmas de dezenas de alunos é aceitável; se crescer muito, dá para carregar rodadas e exercícios
     * uma vez só e reaproveitar.
     */
    public SituacaoTrilha situacaoDe(Aluno aluno) {
        return trilhaService.calcularSituacao(new UsuarioLogado(aluno.getId(), TipoUsuario.ALUNO));
    }

    public static int rodadasAprovadas(SituacaoTrilha situacao) {
        return (int) situacao.statusPorRodada().values().stream()
                .filter(status -> status == StatusRodada.APROVADA)
                .count();
    }

    /** R11: a tarefa "herda" o status da rodada na trilha do aluno. */
    public static StatusTarefa statusDa(Tarefa tarefa, SituacaoTrilha situacao) {
        StatusRodada status = situacao.statusDa(tarefa.getRodada().getId());
        if (status == StatusRodada.APROVADA) return StatusTarefa.CONCLUIDA;
        if (status == StatusRodada.BLOQUEADA) return StatusTarefa.BLOQUEADA;
        return StatusTarefa.PENDENTE;
    }

    /** R12: atrasada = tem prazo, o prazo já passou (ontem ou antes) e ainda não foi concluída. */
    public static boolean atrasada(Tarefa tarefa, StatusTarefa status, LocalDate hoje) {
        return status != StatusTarefa.CONCLUIDA && tarefa.getPrazo() != null && tarefa.getPrazo().isBefore(hoje);
    }

    /**
     * R10: mesmas regras do ranking global (mais XP primeiro; empate pelo username; empatados dividem a posição),
     * mas com TODOS os membros ativos, inclusive quem ainda tem 0 XP. Nenhum e-mail sai daqui.
     *
     * @param voceId id de quem está vendo (para marcar "voce"); null quando é o professor
     */
    public static RankingTurmaResponse ranking(List<Aluno> membros, Integer voceId) {
        List<Aluno> ordenados = membros.stream()
                .sorted(Comparator.comparing(Aluno::getPontuacaoAcumulada, Comparator.reverseOrder())
                        .thenComparing(Aluno::getUsername))
                .toList();
        List<Integer> posicoes = RegrasRanking.posicoes(ordenados.stream().map(Aluno::getPontuacaoAcumulada).toList());

        List<PosicaoRankingResponse> linhas = new ArrayList<>();
        for (int i = 0; i < ordenados.size(); i++) {
            Aluno aluno = ordenados.get(i);
            linhas.add(new PosicaoRankingResponse(posicoes.get(i), aluno.getUsername(), aluno.getPontuacaoAcumulada(),
                    aluno.getNivelAtual(), aluno.getId().equals(voceId), aluno.getAvatar()));
        }
        return new RankingTurmaResponse(linhas);
    }
}
