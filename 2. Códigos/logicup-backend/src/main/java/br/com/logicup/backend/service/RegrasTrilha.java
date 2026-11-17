package br.com.logicup.backend.service;

import br.com.logicup.backend.model.StatusRodada;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        return calcularStatus(aprovadasEmOrdem, aprovadasEmOrdem.stream().map(aprovada -> false).toList());
    }

    /**
     * Sprint 11 (nivelamento): igual ao de cima, mas algumas rodadas podem ter sido "liberadas pelo
     * nivelamento". Para DESBLOQUEAR a seguinte, essas contam como cumpridas; elas mesmas ficam LIBERADAS
     * (e não APROVADAS), porque o aluno ainda não as fez: XP, gemas e conquistas só vêm fazendo.
     *
     * @param liberadasPeloNivelamento para cada rodada, na mesma ordem, se o nivelamento a liberou
     */
    public static List<StatusRodada> calcularStatus(List<Boolean> aprovadasEmOrdem,
                                                    List<Boolean> liberadasPeloNivelamento) {
        if (aprovadasEmOrdem.size() != liberadasPeloNivelamento.size()) {
            throw new IllegalArgumentException("As duas listas precisam ter uma posição por rodada");
        }
        List<StatusRodada> status = new ArrayList<>();
        boolean anteriorCumprida = true; // "antes" da primeira rodada não há nada a cumprir
        for (int i = 0; i < aprovadasEmOrdem.size(); i++) {
            boolean aprovada = aprovadasEmOrdem.get(i);
            boolean nivelada = liberadasPeloNivelamento.get(i);
            if (aprovada) {
                status.add(StatusRodada.APROVADA);
            } else {
                status.add(anteriorCumprida || nivelada ? StatusRodada.LIBERADA : StatusRodada.BLOQUEADA);
            }
            anteriorCumprida = aprovada || nivelada;
        }
        return status;
    }

    /** Sprint 11: a fase (ordenação 1, 2, 3...) está entre as liberadas pelo nivelamento? */
    public static boolean faseLiberadaPeloNivelamento(int ordenacaoDaFase, int fasesLiberadas) {
        return ordenacaoDaFase >= 1 && ordenacaoDaFase <= fasesLiberadas;
    }

    /**
     * Sprint 11 (nivelamento): quantas fases o aluno DOMINOU EM SEQUÊNCIA, a partir da Fase 1.
     * Uma fase é dominada quando o aluno acertou TODAS as questões dela. Na primeira fase não dominada,
     * a contagem para: quem erra a Fase 2 começa na Fase 2, mesmo que acerte a Fase 3 (o conteúdo é
     * cumulativo, e um "buraco" no começo atrapalharia o resto).
     *
     * @param acertouPorFase fase (1, 2, 3...) -> se acertou cada questão daquela fase
     */
    public static int fasesDominadasEmSequencia(Map<Integer, List<Boolean>> acertouPorFase) {
        int fases = 0;
        for (int fase = 1; acertouPorFase.containsKey(fase); fase++) {
            List<Boolean> acertos = acertouPorFase.get(fase);
            if (acertos.isEmpty() || acertos.contains(false)) {
                break;
            }
            fases = fase;
        }
        return fases;
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
