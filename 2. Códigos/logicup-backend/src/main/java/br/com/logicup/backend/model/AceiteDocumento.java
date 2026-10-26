package br.com.logicup.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Sprint 8: "o usuário X aceitou o documento Y na versão Z, em tal data" (tabela aceite_documento).
 * Uma linha por versão: quando os Termos mudarem, o novo aceite é OUTRA linha e o histórico fica.
 * Não guarda IP nem navegador (minimização de dados).
 */
@Entity
@Table(name = "aceite_documento")
public class AceiteDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_aceite")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentoLegal documento;

    @Column(nullable = false, length = 20) // versão vigente NO SERVIDOR no momento do aceite
    private String versao;

    @Column(name = "aceito_em", insertable = false, updatable = false) // o banco preenche (DEFAULT CURRENT_TIMESTAMP)
    private LocalDateTime aceitoEm;

    public AceiteDocumento() {} // exigido pelo Hibernate

    public AceiteDocumento(Usuario usuario, DocumentoLegal documento, String versao) {
        this.usuario = usuario;
        this.documento = documento;
        this.versao = versao;
    }

    public Integer getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public DocumentoLegal getDocumento() { return documento; }
    public String getVersao() { return versao; }
    public LocalDateTime getAceitoEm() { return aceitoEm; }
}
