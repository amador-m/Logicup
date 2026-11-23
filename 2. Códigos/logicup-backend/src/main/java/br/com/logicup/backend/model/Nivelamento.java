package br.com.logicup.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

/**
 * Sprint 11: o resultado do nivelamento de UM aluno (tabela nivelamento, migração 13).
 *
 * A chave primária é o próprio id do aluno (id_usuario): por isso o banco garante que existe no máximo
 * UM registro por aluno. O nivelamento acontece uma vez só, seja fazendo o teste, seja escolhendo começar
 * do zero. Não há setters: o registro nasce pronto e não muda mais.
 *
 * Persistable: o id é escolhido por nós (o id do aluno), e não gerado pelo banco. Sem isso, o save() do
 * Spring Data acharia que o registro "já existe" e faria um merge (SELECT + UPDATE), que poderia
 * SOBRESCREVER um nivelamento gravado por um envio duplicado. Com isNew() = true, o save() faz um INSERT
 * de verdade, e a chave primária do banco recusa o segundo (vira 409 Conflito).
 */
@Entity
@Table(name = "nivelamento")
public class Nivelamento implements Persistable<Integer> {

    @Id
    @Column(name = "id_usuario")
    private Integer alunoId;

    @Column(name = "fez_teste", nullable = false)
    private boolean fezTeste;

    @Column(nullable = false)
    private int acertos;

    @Column(nullable = false)
    private int total;

    @Column(name = "fases_liberadas", nullable = false)
    private int fasesLiberadas;

    @Column(name = "realizado_em", nullable = false)
    private LocalDateTime realizadoEm;

    @Transient // não é coluna: só diz se o objeto ainda não foi gravado
    private boolean novo = true;

    protected Nivelamento() {} // exigido pelo Hibernate

    private Nivelamento(Integer alunoId, boolean fezTeste, int acertos, int total, int fasesLiberadas,
                        LocalDateTime realizadoEm) {
        this.alunoId = alunoId;
        this.fezTeste = fezTeste;
        this.acertos = acertos;
        this.total = total;
        this.fasesLiberadas = fasesLiberadas;
        this.realizadoEm = realizadoEm;
    }

    /** O aluno fez o teste. */
    public static Nivelamento comTeste(Integer alunoId, int acertos, int total, int fasesLiberadas,
                                       LocalDateTime agora) {
        return new Nivelamento(alunoId, true, acertos, total, fasesLiberadas, agora);
    }

    /** O aluno preferiu começar do zero (não libera nada, mas registra que a escolha já foi feita). */
    public static Nivelamento semTeste(Integer alunoId, LocalDateTime agora) {
        return new Nivelamento(alunoId, false, 0, 0, 0, agora);
    }

    @PostLoad
    @PostPersist
    void marcarComoGravado() {
        this.novo = false;
    }

    @Override
    public Integer getId() { return alunoId; }

    @Override
    public boolean isNew() { return novo; }

    public Integer getAlunoId() { return alunoId; }
    public boolean isFezTeste() { return fezTeste; }
    public int getAcertos() { return acertos; }
    public int getTotal() { return total; }
    public int getFasesLiberadas() { return fasesLiberadas; }
    public LocalDateTime getRealizadoEm() { return realizadoEm; }
}
