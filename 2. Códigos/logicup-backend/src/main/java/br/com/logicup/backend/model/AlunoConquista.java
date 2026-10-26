package br.com.logicup.backend.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Sprint 9: uma medalha que um aluno conquistou (tabela aluno_conquista).
 *
 * Chave primária COMPOSTA (aluno + conquista + nível): o BANCO impede ganhar a mesma medalha duas vezes.
 * A gravação é feita pelo AlunoConquistaRepository.registrar() (INSERT IGNORE), e não por save():
 * assim, se duas requisições tentarem gravar a mesma medalha ao mesmo tempo, uma delas simplesmente
 * "não insere nada" (0 linhas) e não paga as gemas de novo.
 * Esta entidade é usada para LER (listar conquistas e as ainda não vistas).
 *
 * @IdClass: a chave são três campos simples desta própria classe (a outra forma, @EmbeddedId, foi usada no AlunoTurma).
 */
@Entity
@Table(name = "aluno_conquista")
@IdClass(AlunoConquista.Chave.class)
public class AlunoConquista {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Id
    @Column(name = "codigo_conquista", length = 40)
    private String codigoConquista;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel")
    private NivelConquista nivel;

    @Column(name = "gemas_recebidas", nullable = false)
    private Integer gemasRecebidas;

    @Column(name = "desbloqueada_em", nullable = false)
    private LocalDateTime desbloqueadaEm;

    @Column(name = "vista_em") // NULL = o aluno ainda não viu a comemoração (decisão M3)
    private LocalDateTime vistaEm;

    protected AlunoConquista() {} // exigido pelo Hibernate

    public Integer getIdUsuario() { return idUsuario; }
    public String getCodigoConquista() { return codigoConquista; }
    public NivelConquista getNivel() { return nivel; }
    public Integer getGemasRecebidas() { return gemasRecebidas; }
    public LocalDateTime getDesbloqueadaEm() { return desbloqueadaEm; }
    public LocalDateTime getVistaEm() { return vistaEm; }

    /** A chave composta. O JPA exige: Serializable, construtor vazio, equals e hashCode. */
    public static class Chave implements Serializable {
        private Integer idUsuario;
        private String codigoConquista;
        private NivelConquista nivel;

        public Chave() {}

        public Chave(Integer idUsuario, String codigoConquista, NivelConquista nivel) {
            this.idUsuario = idUsuario;
            this.codigoConquista = codigoConquista;
            this.nivel = nivel;
        }

        @Override
        public boolean equals(Object outro) {
            return outro instanceof Chave chave && Objects.equals(idUsuario, chave.idUsuario)
                    && Objects.equals(codigoConquista, chave.codigoConquista) && nivel == chave.nivel;
        }

        @Override
        public int hashCode() {
            return Objects.hash(idUsuario, codigoConquista, nivel);
        }
    }
}
