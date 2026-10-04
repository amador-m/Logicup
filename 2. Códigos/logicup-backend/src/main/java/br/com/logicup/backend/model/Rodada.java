package br.com.logicup.backend.model;

import jakarta.persistence.*;

/**
 * Rodada = unidade completa de aprendizagem dentro de um módulo (D2):
 * teoria + exemplos (conteudoTeorico) e, depois, os exercícios da rodada.
 */
@Entity
@Table(name = "rodada")
public class Rodada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rodada")
    private Integer id;

    // Muitas rodadas pertencem a um mesmo módulo
    // LAZY: o módulo só é buscado no banco quando alguém chamar getModulo()
    // optional = false: toda rodada PRECISA de um módulo (id_modulo NOT NULL)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_modulo", nullable = false)
    private Modulo modulo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false) // posição da rodada dentro do módulo (1, 2...)
    private Integer ordenacao;

    // Coluna do tipo JSON no MySQL. No Java guardamos o texto JSON como String;
    // quem transforma esse texto em objetos (seções de teoria, exemplos) é a camada de Service/DTO.
    // columnDefinition = "json" diz ao Hibernate qual é o tipo real da coluna (usado no validate).
    // Formato: {"objetivos":["..."], "teoria":[{"titulo":"...","texto":"..."}], "exemplos":[{"titulo":"...","texto":"...","codigo":"..."}]}
    @Column(name = "conteudo_teorico", nullable = false, columnDefinition = "json")
    private String conteudoTeorico;

    public Rodada() {}

    public Rodada(Modulo modulo, String titulo, Integer ordenacao, String conteudoTeorico) {
        this.modulo = modulo;
        this.titulo = titulo;
        this.ordenacao = ordenacao;
        this.conteudoTeorico = conteudoTeorico;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Modulo getModulo() { return modulo; }
    public void setModulo(Modulo modulo) { this.modulo = modulo; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Integer getOrdenacao() { return ordenacao; }
    public void setOrdenacao(Integer ordenacao) { this.ordenacao = ordenacao; }

    public String getConteudoTeorico() { return conteudoTeorico; }
    public void setConteudoTeorico(String conteudoTeorico) { this.conteudoTeorico = conteudoTeorico; }
}