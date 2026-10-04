package br.com.logicup.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "turma")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turma")
    private Integer id;

    @Column(name = "nome_turma", nullable = false, length = 100)
    private String nomeTurma;

    @ManyToOne // muitas turmas podem apontar pro mesmo professor
    @JoinColumn(name = "id_professor") // diz quel coluna na tabela turma guardas a FK
    private Usuario professor;
    // se o professor for excluído a turma não é apagada junto, só fica orfã

    public Turma() {}

    public Turma(String nomeTurma, Usuario professor) {
        this.nomeTurma = nomeTurma;
        this.professor = professor;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    public Usuario getProfessor() { return professor; }
    public void setProfessor(Usuario professor) { this.professor = professor; }
}