package br.com.logicup.backend.model;

import jakarta.persistence.*;

/**
 * Exercício (questão) de uma rodada. Espelha os campos do Banco de Questões (D1).
 *
 * ATENÇÃO: gabaritoJson é informação SECRETA. Esta entidade nunca deve ser
 * devolvida diretamente pela API — sempre por um DTO que não contenha o gabarito.
 */
@Entity
@Table(name = "exercicio")
public class Exercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_exercicio")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rodada", nullable = false)
    private Rodada rodada;

    // identificador do Banco de Questões: "F1.Q3", "F9.R2.Q5"
    @Column(name = "codigo_questao", nullable = false, unique = true, length = 20)
    private String codigoQuestao;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false) // posição da questão dentro da rodada (1..10)
    private Integer ordenacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "dificuldade") // pode ser NULL (S3)
    private Dificuldade dificuldade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_exercicio", nullable = false)
    private TipoExercicio tipoExercicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "formato_resposta", nullable = false)
    private FormatoResposta formatoResposta;

    @Column(nullable = false) // XP da questão
    private Integer pontos;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String enunciado;

    @Column(name = "codigo_apoio", columnDefinition = "TEXT") // trecho de código exibido com o enunciado
    private String codigoApoio;

    @Column(name = "alternativas_json", columnDefinition = "json") // PÚBLICO: vai para o frontend
    private String alternativasJson;

    @Column(name = "gabarito_json", nullable = false, columnDefinition = "json") // SECRETO: nunca vai para o frontend
    private String gabaritoJson;

    @Column(nullable = false, columnDefinition = "TEXT") // mostrada depois da correção
    private String explicacao;

    @Column(name = "template_inicial_java", columnDefinition = "TEXT")
    private String templateInicialJava;

    @Column(name = "estrutura_blocos_json", columnDefinition = "json")
    private String estruturaBlocosJson;

    @Column(name = "codigo_teste_junit", columnDefinition = "TEXT") // NULL até existir o motor JUnit (D1)
    private String codigoTesteJunit;

    public Exercicio() {}

    // construtor com os campos obrigatórios (NOT NULL); os opcionais são preenchidos por setters
    public Exercicio(Rodada rodada, String codigoQuestao, String titulo, Integer ordenacao,
                     TipoExercicio tipoExercicio, FormatoResposta formatoResposta, Integer pontos,
                     String enunciado, String gabaritoJson, String explicacao) {
        this.rodada = rodada;
        this.codigoQuestao = codigoQuestao;
        this.titulo = titulo;
        this.ordenacao = ordenacao;
        this.tipoExercicio = tipoExercicio;
        this.formatoResposta = formatoResposta;
        this.pontos = pontos;
        this.enunciado = enunciado;
        this.gabaritoJson = gabaritoJson;
        this.explicacao = explicacao;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Rodada getRodada() { return rodada; }
    public void setRodada(Rodada rodada) { this.rodada = rodada; }

    public String getCodigoQuestao() { return codigoQuestao; }
    public void setCodigoQuestao(String codigoQuestao) { this.codigoQuestao = codigoQuestao; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Integer getOrdenacao() { return ordenacao; }
    public void setOrdenacao(Integer ordenacao) { this.ordenacao = ordenacao; }

    public Dificuldade getDificuldade() { return dificuldade; }
    public void setDificuldade(Dificuldade dificuldade) { this.dificuldade = dificuldade; }

    public TipoExercicio getTipoExercicio() { return tipoExercicio; }
    public void setTipoExercicio(TipoExercicio tipoExercicio) { this.tipoExercicio = tipoExercicio; }

    public FormatoResposta getFormatoResposta() { return formatoResposta; }
    public void setFormatoResposta(FormatoResposta formatoResposta) { this.formatoResposta = formatoResposta; }

    public Integer getPontos() { return pontos; }
    public void setPontos(Integer pontos) { this.pontos = pontos; }

    public String getEnunciado() { return enunciado; }
    public void setEnunciado(String enunciado) { this.enunciado = enunciado; }

    public String getCodigoApoio() { return codigoApoio; }
    public void setCodigoApoio(String codigoApoio) { this.codigoApoio = codigoApoio; }

    public String getAlternativasJson() { return alternativasJson; }
    public void setAlternativasJson(String alternativasJson) { this.alternativasJson = alternativasJson; }

    public String getGabaritoJson() { return gabaritoJson; }
    public void setGabaritoJson(String gabaritoJson) { this.gabaritoJson = gabaritoJson; }

    public String getExplicacao() { return explicacao; }
    public void setExplicacao(String explicacao) { this.explicacao = explicacao; }

    public String getTemplateInicialJava() { return templateInicialJava; }
    public void setTemplateInicialJava(String templateInicialJava) { this.templateInicialJava = templateInicialJava; }

    public String getEstruturaBlocosJson() { return estruturaBlocosJson; }
    public void setEstruturaBlocosJson(String estruturaBlocosJson) { this.estruturaBlocosJson = estruturaBlocosJson; }

    public String getCodigoTesteJunit() { return codigoTesteJunit; }
    public void setCodigoTesteJunit(String codigoTesteJunit) { this.codigoTesteJunit = codigoTesteJunit; }
}