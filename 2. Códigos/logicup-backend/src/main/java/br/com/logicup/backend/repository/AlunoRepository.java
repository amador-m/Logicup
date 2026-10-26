package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Aluno;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /*
     * Sprint 9 (decisão M2-A): o saldo de gemas SÓ muda por estes UPDATEs atômicos.
     * "gemas = gemas + :valor" é calculado PELO BANCO no momento da gravação, e não a partir de um valor
     * lido antes pelo Java. Assim duas operações simultâneas nunca "apagam" uma à outra.
     * nativeQuery: SQL puro, direto na tabela aluno (a coluna gemas não é gravada pelo Hibernate).
     * @Modifying: avisa o Spring Data que a consulta ALTERA dados (executeUpdate) e devolve quantas linhas mudaram.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE aluno SET gemas = gemas + :valor WHERE id_usuario = :alunoId", nativeQuery = true)
    int creditarGemas(@Param("alunoId") Integer alunoId, @Param("valor") int valor);

    /**
     * Débito da compra: só acontece se HOUVER saldo, na mesma instrução.
     * @return 1 se debitou; 0 se o saldo não era suficiente (nada muda)
     */
    @Modifying(flushAutomatically = true)
    @Query(value = "UPDATE aluno SET gemas = gemas - :valor WHERE id_usuario = :alunoId AND gemas >= :valor",
            nativeQuery = true)
    int debitarGemas(@Param("alunoId") Integer alunoId, @Param("valor") int valor);

    /** Saldo atual lido DO BANCO (o campo Aluno.gemas em memória pode estar desatualizado). */
    @Query(value = "SELECT gemas FROM aluno WHERE id_usuario = :alunoId", nativeQuery = true)
    Integer saldoDeGemas(@Param("alunoId") Integer alunoId);
}