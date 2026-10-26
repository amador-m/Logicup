package br.com.logicup.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    // Sprint 9: o banco preenche na criação da matrícula (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "entrou_em", insertable = false, updatable = false)
    private LocalDateTime entrouEm;

    // Sprint 9: quando saiu, foi removido ou voltou (null enquanto nada mudou)
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

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

    public LocalDateTime getEntrouEm() { return entrouEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }

    public boolean isAtiva() { return status == StatusMatricula.ATIVO; }

    /*
     * Sprint 9: as mudanças de status são MÉTODOS com nome de regra, e não um setStatus solto:
     * assim fica claro no código que existem só três transições possíveis (e cada uma registra quando).
     */
    public void sair(LocalDateTime agora) { mudarPara(StatusMatricula.SAIU, agora); }          // R6
    public void remover(LocalDateTime agora) { mudarPara(StatusMatricula.REMOVIDO, agora); }    // R7
    public void reativar(LocalDateTime agora) { mudarPara(StatusMatricula.ATIVO, agora); }      // R6: voltou

    private void mudarPara(StatusMatricula novo, LocalDateTime agora) {
        this.status = novo;
        this.atualizadoEm = agora;
    }
}