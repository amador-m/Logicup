package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    // SELECT * FROM modulo ORDER BY ordenacao ASC  -> monta a trilha na ordem das fases
    List<Modulo> findAllByOrderByOrdenacaoAsc();

    // SELECT * FROM modulo WHERE ordenacao = ?  -> "me dá a Fase 3"
    Optional<Modulo> findByOrdenacao(Integer ordenacao);
}