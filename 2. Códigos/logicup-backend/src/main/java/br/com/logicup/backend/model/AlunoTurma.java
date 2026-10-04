package br.com.logicup.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "aluno_turma")
public class AlunoTurma {

    @EmbeddedId // para mostrar que a PK dessa entidade é o obj. composto criado em AlunoTurmaId
    private AlunoTurmaId id;

    @ManyToOne
    @MapsId("idUsuario") // o valor do relacionamento (aluno) é usado pra preencher a PK do usuário
    @JoinColumn(name = "id_usuario")
    private Aluno aluno;

    @ManyToOne
    @MapsId("idTurma")
    @JoinColumn(name = "id_turma")
    private Turma turma;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusMatricula status = StatusMatricula.ATIVO; // default ativo

    public AlunoTurma() {}

    public AlunoTurma(Aluno aluno, Turma turma) {
        this.aluno = aluno;
        this.turma = turma;
        this.id = new AlunoTurmaId(aluno.getId(), turma.getId());
        this.status = StatusMatricula.ATIVO;
    }

    // sem set pra aluno/turmo pois a conta é criada ligando o aluno à turma, ent não faz sentido trocar tais vinculos depois (seria melhor criar outra matrícula)
    public AlunoTurmaId getId() { return id; }

    public Aluno getAluno() { return aluno; }
    public Turma getTurma() { return turma; }

    public StatusMatricula getStatus() { return status; }
    public void setStatus(StatusMatricula status) { this.status = status; }
}