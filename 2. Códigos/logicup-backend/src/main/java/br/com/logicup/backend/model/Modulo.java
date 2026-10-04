package br.com.logicup.backend.model;

import jakarta.persistence.*;

/**
 * Módulo = uma FASE da trilha (Fase 1: Lógica de Programação, Fase 2: ...).
 * Ele apenas agrupa rodadas; o conteúdo teórico e os exercícios ficam na Rodada.
 *
 * O relacionamento com Rodada é UNIDIRECIONAL: quem conhece o módulo é a rodada
 * (Rodada -> Modulo). Para listar as rodadas de um módulo usamos o RodadaRepository.
 */
@Entity
@Table(name = "modulo")
public class Modulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modulo")
    private Integer id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 255) // opcional (pode ser NULL no banco)
    private String descricao;

    @Column(nullable = false, unique = true) // é o número da fase: 1, 2, 3... (UNIQUE no banco)
    private Integer ordenacao;

    public Modulo() {} // exigido pelo Hibernate

    public Modulo(String titulo, String descricao, Integer ordenacao) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.ordenacao = ordenacao;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getOrdenacao() { return ordenacao; }
    public void setOrdenacao(Integer ordenacao) { this.ordenacao = ordenacao; }
}