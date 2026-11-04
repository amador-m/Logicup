package br.com.logicup.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Sprint 10: um código de uso único enviado por e-mail (tabela token_usuario).
 *
 * O código em si NUNCA é gravado, só o hash SHA-256 dele (hash_token). Funciona como uma senha:
 * se alguém conseguir ler o banco (um backup vazado, por exemplo), não consegue montar os links.
 * Quando a pessoa clica no link, calculamos o hash do código recebido e procuramos esse hash aqui.
 *
 * Regras no próprio objeto (como em ProgressoExercicio): sem setters de estado; só usar().
 */
@Entity
@Table(name = "token_usuario")
public class TokenUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FinalidadeToken finalidade;

    @Column(name = "hash_token", nullable = false, unique = true, length = 64)
    private String hashToken;

    // preenchido pelo Java com o MESMO "agora" do expira_em: as duas datas sempre no mesmo fuso
    // (o DEFAULT do banco usaria o relógio do MySQL, que pode estar em outro fuso)
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "usado_em")
    private LocalDateTime usadoEm;

    protected TokenUsuario() {} // exigência do Hibernate

    public TokenUsuario(Usuario usuario, FinalidadeToken finalidade, String hashToken, LocalDateTime agora) {
        this.usuario = usuario;
        this.finalidade = finalidade;
        this.hashToken = hashToken;
        this.criadoEm = agora;
        this.expiraEm = agora.plus(finalidade.getValidade());
    }

    /** Ainda serve? (não foi usado e não venceu) */
    public boolean valeEm(LocalDateTime agora) {
        return usadoEm == null && agora.isBefore(expiraEm);
    }

    /** Marca como usado. Cada link vale UMA vez: depois disso, valeEm() é sempre false. */
    public void usar(LocalDateTime agora) {
        if (!valeEm(agora)) {
            throw new IllegalStateException("Token já usado ou vencido");
        }
        this.usadoEm = agora;
    }

    /**
     * Tira de circulação um código que ainda não foi usado (pediram um link novo, ou outro link da mesma
     * finalidade foi usado). Diferente de usar(): vale também para código vencido. Marca, não apaga:
     * o histórico ajuda a investigar abuso.
     */
    public void aposentar(LocalDateTime agora) {
        if (usadoEm == null) {
            this.usadoEm = agora;
        }
    }

    public Integer getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public FinalidadeToken getFinalidade() { return finalidade; }
    public LocalDateTime getExpiraEm() { return expiraEm; }
    public LocalDateTime getUsadoEm() { return usadoEm; }
}
