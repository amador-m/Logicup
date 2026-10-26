package br.com.logicup.backend.model;

/**
 * Sprint 9 (conquistas): o nível de uma medalha e quantas gemas ela vale (decisão M4).
 * UNICA = conquista que só tem um nível (ex.: "Primeiro acerto").
 *
 * A ORDEM aqui importa só para exibição (bronze antes de prata antes de ouro).
 * No banco é a coluna aluno_conquista.nivel, ENUM('UNICA','BRONZE','PRATA','OURO').
 */
public enum NivelConquista {

    UNICA(15),
    BRONZE(15),
    PRATA(30),
    OURO(60);

    private final int gemas;

    NivelConquista(int gemas) {
        this.gemas = gemas;
    }

    public int getGemas() {
        return gemas;
    }
}
