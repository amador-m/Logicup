package br.com.logicup.backend.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Sprint 9: o CATÁLOGO de conquistas (decisão M1-A: fica no código, não no banco).
 * O banco guarda só o código (o nome da constante, ex.: "RODADAS") em aluno_conquista.codigo_conquista.
 *
 * Cada conquista tem uma MÉTRICA (o que é contado) e um ou mais níveis com a META de cada um.
 * Mudar uma meta aqui não exige migração; mas medalhas já ganhas NUNCA são retiradas.
 *
 * Metas aprovadas na decisão C2 (22/10).
 */
public enum Conquista {

    PRIMEIRO_ACERTO("Primeiro acerto", "Acerte sua primeira questão.",
            Metrica.QUESTOES_CONCLUIDAS, new Meta(NivelConquista.UNICA, 1)),

    RODADAS("Rodada após rodada", "Seja aprovado em %d rodada(s).",
            Metrica.RODADAS_APROVADAS, bronzePrataOuro(1, 5, 15)),

    XP("Caçador de XP", "Junte %d XP.",
            Metrica.XP, bronzePrataOuro(100, 500, 1500)),

    OFENSIVA("Em chamas", "Estude %d dias seguidos.",
            Metrica.OFENSIVA, bronzePrataOuro(3, 7, 30)),

    ACERTO_DE_PRIMEIRA("Na mosca", "Acerte %d questões de primeira.",
            Metrica.ACERTOS_DE_PRIMEIRA, bronzePrataOuro(5, 25, 75)),

    BOSS("Caçador de Boss", "Vença %d desafio(s) Boss.",
            Metrica.BOSSES_VENCIDOS, bronzePrataOuro(1, 3, 10)),

    FASES("Desbravador", "Conclua %d fase(s) inteira(s).",
            Metrica.FASES_CONCLUIDAS, bronzePrataOuro(1, 3, 10)),

    TURMA("Colega de turma", "Entre em uma turma com o código do professor.",
            Metrica.TURMA, new Meta(NivelConquista.UNICA, 1));

    /** O que cada conquista conta. Os valores são calculados no RegrasConquistas. */
    public enum Metrica {
        QUESTOES_CONCLUIDAS, RODADAS_APROVADAS, XP, OFENSIVA, ACERTOS_DE_PRIMEIRA,
        BOSSES_VENCIDOS, FASES_CONCLUIDAS, TURMA
    }

    /** Um nível e o valor mínimo da métrica para ganhá-lo. */
    public record Meta(NivelConquista nivel, int valor) {}

    private final String titulo;
    private final String descricao; // pode ter %d, trocado pela meta de cada nível
    private final Metrica metrica;
    private final List<Meta> metas;

    Conquista(String titulo, String descricao, Metrica metrica, Meta... metas) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.metrica = metrica;
        this.metas = List.of(metas);
    }

    private static Meta[] bronzePrataOuro(int bronze, int prata, int ouro) {
        return new Meta[]{new Meta(NivelConquista.BRONZE, bronze), new Meta(NivelConquista.PRATA, prata),
                new Meta(NivelConquista.OURO, ouro)};
    }

    /** Código vindo do banco -> conquista. Vazio se o código não existe mais no catálogo. */
    public static Optional<Conquista> porCodigo(String codigo) {
        return Arrays.stream(values()).filter(conquista -> conquista.name().equals(codigo)).findFirst();
    }

    public String getTitulo() { return titulo; }
    public Metrica getMetrica() { return metrica; }
    public List<Meta> getMetas() { return metas; }

    /** Descrição de um nível, já com a meta (ex.: "Junte 500 XP."). */
    public String descricaoDe(Meta meta) {
        return descricao.contains("%d") ? descricao.formatted(meta.valor()) : descricao;
    }
}
