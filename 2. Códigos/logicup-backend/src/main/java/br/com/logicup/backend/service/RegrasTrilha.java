package br.com.logicup.backend.service;

import br.com.logicup.backend.model.StatusRodada;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Regras PURAS da trilha (sem banco, sem Spring): recebem dados e devolvem decisões.
 * Por serem puras, são testadas com testes de unidade simples (RegrasTrilhaTest).
 */
public final class RegrasTrilha {

    private static final int PERCENTUAL_APROVACAO = 80; // PR5

    private RegrasTrilha() {}

    /** O mínimo que as regras precisam saber de um exercício. */
    public record ExercicioDaRodada(Integer id, boolean boss) {}

    /**
     * PR5: aprovada quando >= 80% dos exercícios estão concluídos E todo Boss da rodada está concluído.
     * Conta com inteiros (feitos * 100 >= total * 80) para evitar arredondamento de ponto flutuante.
     */
    public static boolean rodadaAprovada(List<ExercicioDaRodada> exercicios, Set<Integer> concluidos) {
        if (exercicios.isEmpty()) {
            return false; // rodada sem exercícios nunca é aprovada (conteúdo incompleto)
        }
        long feitos = exercicios.stream().filter(e -> concluidos.contains(e.id())).count();
        boolean bossConcluido = exercicios.stream()
                .filter(ExercicioDaRodada::boss)
                .allMatch(e -> concluidos.contains(e.id()));
        return bossConcluido && feitos * 100 >= (long) exercicios.size() * PERCENTUAL_APROVACAO;
    }

    /**
     * Desbloqueio em SEQUÊNCIA (por toda a trilha, atravessando as fases):
     * a primeira rodada é sempre liberada; cada rodada seguinte libera quando a anterior foi aprovada.
     *
     * @param aprovadasEmOrdem para cada rodada, na ordem da trilha, se ela está aprovada
     */
    public static List<StatusRodada> calcularStatus(List<Boolean> aprovadasEmOrdem) {
        List<StatusRodada> status = new ArrayList<>();
        boolean anteriorAprovada = true; // "antes" da primeira rodada não há nada a cumprir
        for (boolean aprovada : aprovadasEmOrdem) {
            if (aprovada) {
                status.add(StatusRodada.APROVADA);
            } else {
                status.add(anteriorAprovada ? StatusRodada.LIBERADA : StatusRodada.BLOQUEADA);
            }
            anteriorAprovada = aprovada;
        }
        return status;
    }

    /**
     * N-A: nível = número (ordenação) da fase mais alta que tem alguma rodada não bloqueada.
     * Nunca menor que 1.
     *
     * @param ordemDaFase para cada rodada, na ordem da trilha, a ordenação da fase dela
     */
    public static int faseMaisAltaLiberada(List<Integer> ordemDaFase, List<StatusRodada> status) {
        int maior = 1;
        for (int i = 0; i < status.size(); i++) {
            if (status.get(i) != StatusRodada.BLOQUEADA) {
                maior = Math.max(maior, ordemDaFase.get(i));
            }
        }
        return maior;
    }
}
