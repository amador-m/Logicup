package br.com.logicup.backend.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Sprint 9: uma rodada da trilha atribuída pelo professor a uma turma (R11).
 * A tarefa NÃO guarda progresso: "concluída" é calculado a partir da rodada aprovada na trilha.
 * Por isso remover a tarefa nunca apaga o que o aluno fez (R13).
 */
@Entity
@Table(name = "tarefa")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tarefa")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_turma", nullable = false)
    private Turma turma;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rodada", nullable = false)
    private Rodada rodada;

    @Column(name = "prazo")
    private LocalDate prazo; // opcional e informativo (R12)

    @Column(name = "criada_em", insertable = false, updatable = false)
    private LocalDateTime criadaEm;

    public Tarefa() {}

    public Tarefa(Turma turma, Rodada rodada, LocalDate prazo) {
        this.turma = turma;
        this.rodada = rodada;
        this.prazo = prazo;
    }

    public Integer getId() { return id; }
    public Turma getTurma() { return turma; }
    public Rodada getRodada() { return rodada; }
    public LocalDate getPrazo() { return prazo; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}
