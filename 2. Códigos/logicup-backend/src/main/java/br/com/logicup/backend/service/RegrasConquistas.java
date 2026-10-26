package br.com.logicup.backend.service;

import br.com.logicup.backend.model.Conquista;
import br.com.logicup.backend.model.Conquista.Meta;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.Rodada;
import br.com.logicup.backend.model.StatusRodada;
import br.com.logicup.backend.model.TipoExercicio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sprint 9: regras PURAS das conquistas (sem banco, sem Spring), testadas no RegrasConquistasTest.
 *
 * Duas etapas:
 *  1) metricas(): transforma a situação da trilha + dados do aluno nos NÚMEROS que as conquistas contam;
 *  2) alcancadas(): diz quais níveis de quais conquistas esses números já alcançaram.
 * Quem grava o resultado (e paga as gemas) é o ConquistaService.
 */
public final class RegrasConquistas {

    private RegrasConquistas() {}

    /** Os números de um aluno que as conquistas observam. */
    public record MetricasAluno(int questoesConcluidas, int rodadasAprovadas, int xp, int ofensiva,
                                int acertosDePrimeira, int bossesVencidos, int fasesConcluidas, boolean entrouEmTurma) {

        public int valorDe(Conquista.Metrica metrica) {
            return switch (metrica) {
                case QUESTOES_CONCLUIDAS -> questoesConcluidas;
                case RODADAS_APROVADAS -> rodadasAprovadas;
                case XP -> xp;
                case OFENSIVA -> ofensiva;
                case ACERTOS_DE_PRIMEIRA -> acertosDePrimeira;
                case BOSSES_VENCIDOS -> bossesVencidos;
                case FASES_CONCLUIDAS -> fasesConcluidas;
                case TURMA -> entrouEmTurma ? 1 : 0;
            };
        }
    }

    /** Uma medalha alcançada (conquista + nível). */
    public record Alcancada(Conquista conquista, Meta meta) {}

    /**
     * @param xp       XP total do aluno (pontuacao_acumulada)
     * @param ofensiva maior ofensiva REGISTRADA na última atividade (streak_atual). Mesmo que a sequência
     *                 tenha quebrado depois, ela foi alcançada: medalha ganha nunca é retirada.
     */
    public static MetricasAluno metricas(TrilhaService.SituacaoTrilha situacao, int xp, int ofensiva,
                                         boolean entrouEmTurma) {
        int acertosDePrimeira = 0;
        int bosses = 0;
        for (List<Exercicio> exercicios : situacao.exerciciosPorRodada().values()) {
            for (Exercicio exercicio : exercicios) {
                if (!situacao.exerciciosConcluidos().contains(exercicio.getId())) {
                    continue;
                }
                // PR7: quem errou antes de acertar ganha só METADE do XP. XP cheio = acertou de primeira.
                // Continua certo mesmo se a rodada for refeita (o XP gravado não muda depois da 1ª conclusão).
                int xpGanho = situacao.xpPorExercicio().getOrDefault(exercicio.getId(), 0);
                int pontos = exercicio.getPontos() != null ? exercicio.getPontos() : 0;
                if (xpGanho >= pontos) {
                    acertosDePrimeira++;
                }
                if (exercicio.getTipoExercicio() == TipoExercicio.BOSS_CHALLENGE) {
                    bosses++;
                }
            }
        }

        int rodadasAprovadas = 0;
        // fase (módulo) -> todas as rodadas dela aprovadas?  LinkedHashMap só para manter a ordem da trilha
        Map<Integer, Boolean> faseCompleta = new LinkedHashMap<>();
        for (Rodada rodada : situacao.rodadas()) {
            boolean aprovada = situacao.statusDa(rodada.getId()) == StatusRodada.APROVADA;
            if (aprovada) {
                rodadasAprovadas++;
            }
            faseCompleta.merge(rodada.getModulo().getId(), aprovada, Boolean::logicalAnd);
        }
        int fasesConcluidas = (int) faseCompleta.values().stream().filter(Boolean::booleanValue).count();

        return new MetricasAluno(situacao.exerciciosConcluidos().size(), rodadasAprovadas, xp, ofensiva,
                acertosDePrimeira, bosses, fasesConcluidas, entrouEmTurma);
    }

    /** Todos os níveis que as métricas já alcançaram, na ordem do catálogo (já ganhos ou não). */
    public static List<Alcancada> alcancadas(MetricasAluno metricas) {
        List<Alcancada> resultado = new ArrayList<>();
        for (Conquista conquista : Conquista.values()) {
            int valor = metricas.valorDe(conquista.getMetrica());
            for (Meta meta : conquista.getMetas()) {
                if (valor >= meta.valor()) {
                    resultado.add(new Alcancada(conquista, meta));
                }
            }
        }
        return resultado;
    }
}
