package br.com.logicup.backend.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Sprint 9: um avatar COMPRADO por um aluno (tabela aluno_avatar).
 * Os avatares gratuitos não têm linha aqui: todo mundo já os possui.
 *
 * Chave composta (aluno + avatar): não dá para comprar o mesmo avatar duas vezes, nem com duas
 * requisições simultâneas (a segunda falha na chave primária e a transação inteira, inclusive o
 * débito das gemas, é desfeita). Esta entidade é usada para LER; a compra é gravada por INSERT nativo.
 * precoPago guarda o preço DA ÉPOCA: se o preço mudar no enum Avatar, o histórico continua correto.
 */
@Entity
@Table(name = "aluno_avatar")
@IdClass(AlunoAvatar.Chave.class)
public class AlunoAvatar {

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Id
    @Column(name = "codigo_avatar", length = 30)
    private String codigoAvatar;

    @Column(name = "preco_pago", nullable = false)
    private Integer precoPago;

    @Column(name = "comprado_em", nullable = false)
    private LocalDateTime compradoEm;

    protected AlunoAvatar() {} // exigido pelo Hibernate; a gravação é pelo AlunoAvatarRepository.registrarCompra()

    public Integer getIdUsuario() { return idUsuario; }
    public String getCodigoAvatar() { return codigoAvatar; }
    public Integer getPrecoPago() { return precoPago; }
    public LocalDateTime getCompradoEm() { return compradoEm; }

    public static class Chave implements Serializable {
        private Integer idUsuario;
        private String codigoAvatar;

        public Chave() {}

        public Chave(Integer idUsuario, String codigoAvatar) {
            this.idUsuario = idUsuario;
            this.codigoAvatar = codigoAvatar;
        }

        @Override
        public boolean equals(Object outro) {
            return outro instanceof Chave chave && Objects.equals(idUsuario, chave.idUsuario)
                    && Objects.equals(codigoAvatar, chave.codigoAvatar);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idUsuario, codigoAvatar);
        }
    }
}
