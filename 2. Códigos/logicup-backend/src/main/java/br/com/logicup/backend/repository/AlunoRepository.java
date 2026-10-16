package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Aluno;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Integer> {

    /*
     * Sprint 7 — ranking: alunos com XP > 0, do maior para o menor; no empate, ordem alfabética do username.
     * "username" é herdado de Usuario: com a herança JOINED, o Hibernate faz o JOIN aluno + usuario sozinho.
     * O Pageable vira o LIMIT do SQL (ex.: PageRequest.of(0, 10) -> só os 10 primeiros).
     */
    @Query("SELECT a FROM Aluno a WHERE a.pontuacaoAcumulada > 0 "
            + "ORDER BY a.pontuacaoAcumulada DESC, a.username ASC")
    List<Aluno> buscarTopoDoRanking(Pageable pagina);

    // SELECT COUNT(*) FROM aluno WHERE pontuacao_acumulada > ?  -> quantos estão À FRENTE (posição = isso + 1)
    long countByPontuacaoAcumuladaGreaterThan(Integer xp);
}