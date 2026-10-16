package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.PosicaoRankingResponse;
import br.com.logicup.backend.dto.RankingResponse;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.security.UsuarioLogado;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Ranking global por XP (Sprint 7).
 * Só ALUNOS: a consulta parte da entidade Aluno, e na herança JOINED a tabela "aluno"
 * só tem linhas de alunos — professor e admin ficam de fora sem nenhum "if".
 */
@Service
public class RankingService {

    static final int TAMANHO_TOPO = 10;

    private final AlunoRepository alunoRepository;

    public RankingService(AlunoRepository alunoRepository) {
        this.alunoRepository = alunoRepository;
    }

    @Transactional(readOnly = true)
    public RankingResponse montar(UsuarioLogado usuario) {
        // 1 consulta: os 10 primeiros (XP > 0), em ordem; empate -> ordem alfabética do username
        List<Aluno> primeiros = alunoRepository.buscarTopoDoRanking(PageRequest.of(0, TAMANHO_TOPO));
        List<Integer> posicoes = RegrasRanking.posicoes(
                primeiros.stream().map(Aluno::getPontuacaoAcumulada).toList());

        List<PosicaoRankingResponse> topo = new ArrayList<>();
        for (int i = 0; i < primeiros.size(); i++) {
            Aluno aluno = primeiros.get(i);
            topo.add(linha(posicoes.get(i), aluno, aluno.getId().equals(usuario.id())));
        }

        return new RankingResponse(topo, usuario.isAluno() ? posicaoDe(usuario.id()) : null);
    }

    /** Posição de quem está logado, mesmo fora do top 10 (1 consulta de contagem). */
    private PosicaoRankingResponse posicaoDe(Integer alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId).orElseThrow(SessaoInvalidaException::new);
        int xp = aluno.getPontuacaoAcumulada();
        Integer posicao = xp > 0
                ? RegrasRanking.posicao(alunoRepository.countByPontuacaoAcumuladaGreaterThan(xp))
                : null; // 0 XP: ainda não entrou na classificação
        return linha(posicao, aluno, true);
    }

    private static PosicaoRankingResponse linha(Integer posicao, Aluno aluno, boolean voce) {
        return new PosicaoRankingResponse(posicao, aluno.getUsername(), aluno.getPontuacaoAcumulada(),
                aluno.getNivelAtual(), voce);
    }
}
