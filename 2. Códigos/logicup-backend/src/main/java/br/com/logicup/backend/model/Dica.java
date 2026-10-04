package br.com.logicup.backend.model;

import jakarta.persistence.*;

/**
 * Dica de um exercício. Um exercício pode ter 0, 1 ou várias dicas.
 * Relacionamento unidirecional (Dica -> Exercicio), mesmo padrão de Rodada -> Modulo.
 * Sem coluna de ordem: quando houver mais de uma dica, a ordem é a de cadastro (id).
 */
@Entity
@Table(name = "dica")
public class Dica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dica")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_exercicio", nullable = false)
    private Exercicio exercicio;

    @Column(name = "texto_dica", nullable = false, length = 255)
    private String textoDica;

    public Dica() {}

    public Dica(Exercicio exercicio, String textoDica) {
        this.exercicio = exercicio;
        this.textoDica = textoDica;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Exercicio getExercicio() { return exercicio; }
    public void setExercicio(Exercicio exercicio) { this.exercicio = exercicio; }

    public String getTextoDica() { return textoDica; }
    public void setTextoDica(String textoDica) { this.textoDica = textoDica; }
}