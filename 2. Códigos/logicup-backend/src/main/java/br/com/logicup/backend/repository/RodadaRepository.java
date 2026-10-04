package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Rodada;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RodadaRepository extends JpaRepository<Rodada, Integer> {

    // "Modulo" + "Id" = navega em rodada.modulo.id
    // SELECT * FROM rodada WHERE id_modulo = ? ORDER BY ordenacao ASC
    List<Rodada> findByModuloIdOrderByOrdenacaoAsc(Integer moduloId);

    /*
     * Todas as rodadas na ordem da trilha (fase 1 rodada 1, fase 1 rodada 2, fase 2 rodada 1...).
     * @EntityGraph("modulo"): traz o módulo JUNTO, num único SELECT com JOIN.
     * Sem isso, como Rodada->Modulo é LAZY, cada rodada dispararia um SELECT extra (problema N+1).
     */
    @EntityGraph(attributePaths = "modulo")
    @Query("SELECT r FROM Rodada r ORDER BY r.modulo.ordenacao ASC, r.ordenacao ASC")
    List<Rodada> findAllEmOrdemDaTrilha();
}
