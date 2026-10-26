package br.com.logicup.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "turma")
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turma")
    private Integer id;

    @Column(name = "nome_turma", nullable = false, length = 100)
    private String nomeTurma;

    // Sprint 9: código de convite (R3). Gerado pelo servidor (GeradorCodigoConvite), nunca pelo cliente.
    @Column(name = "codigo_convite", nullable = false, unique = true, length = 8)
    private String codigoConvite;

    @ManyToOne // muitas turmas podem apontar pro mesmo professor
    @JoinColumn(name = "id_professor") // diz quel coluna na tabela turma guardas a FK
    private Usuario professor;
    // se o professor for excluído a turma não é apagada junto, só fica orfã

    // Sprint 9: o banco preenche na hora do INSERT (DEFAULT CURRENT_TIMESTAMP)
    @Column(name = "criada_em", insertable = false, updatable = false)
    private LocalDateTime criadaEm;

    public Turma() {}

    public Turma(String nomeTurma, Usuario professor, String codigoConvite) {
        this.nomeTurma = nomeTurma;
        this.professor = professor;
        this.codigoConvite = codigoConvite;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    public String getCodigoConvite() { return codigoConvite; }
    public void setCodigoConvite(String codigoConvite) { this.codigoConvite = codigoConvite; }

    public Usuario getProfessor() { return professor; }
    public void setProfessor(Usuario professor) { this.professor = professor; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
}
