package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.QuestaoNivelamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestaoNivelamentoRepository extends JpaRepository<QuestaoNivelamento, Integer> {

    /** As questões na ordem do teste: Fase 1 (questão 1, 2), Fase 2 (1, 2)... */
    List<QuestaoNivelamento> findAllByOrderByFaseAscOrdenacaoAsc();
}
