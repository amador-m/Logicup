package br.com.logicup.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Sprint 11: uma questão do quiz de nivelamento (tabela questao_nivelamento, migração 13).
 * Fica SEPARADA dos exercícios da trilha: responder o nivelamento não gera progresso nas rodadas.
 * Só leitura pela aplicação (as questões entram pela migração).
 */
@Entity
@Table(name = "questao_nivelamento")
public class QuestaoNivelamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_questao")
    private Integer id;

    @Column(nullable = false) // ordenação da fase (modulo.ordenacao) avaliada pela questão
    private Integer fase;

    @Column(nullable = false)
    private Integer ordenacao;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(columnDefinition = "TEXT")
    private String codigo;

    @Column(name = "alternativas_json", nullable = false, columnDefinition = "json") // PÚBLICO
    private String alternativasJson;

    @Column(nullable = false, length = 5) // SECRETO até a correção: nunca vai no DTO das questões
    private String correta;

    @Column(nullable = false, columnDefinition = "TEXT") // mostrada depois da correção
    private String explicacao;

    protected QuestaoNivelamento() {} // exigido pelo Hibernate

    public Integer getId() { return id; }
    public Integer getFase() { return fase; }
    public Integer getOrdenacao() { return ordenacao; }
    public String getEnunciado() { return enunciado; }
    public String getCodigo() { return codigo; }
    public String getAlternativasJson() { return alternativasJson; }
    public String getCorreta() { return correta; }
    public String getExplicacao() { return explicacao; }
}
