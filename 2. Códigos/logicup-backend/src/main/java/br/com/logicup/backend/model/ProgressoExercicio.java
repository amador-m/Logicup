package br.com.logicup.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Estado ATUAL do aluno em um exercício (uma linha por aluno + exercício — UNIQUE no banco).
 * Não guarda histórico de todas as tentativas, só o resumo: status, nº de tentativas,
 * XP ganho e a última resposta enviada.
 *
 * R1 (opção B): as regras PR1, PR2 e PR3 ficam DENTRO desta classe, no método
 * registrarResposta(). Por isso status, tentativas, pontosGanhos e respostaJson
 * não têm setters: a única forma de alterá-los é passando pela regra.
 */
@Entity
@Table(name = "progresso_exercicio")
public class ProgressoExercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progresso")
    private Integer id;

    // a FK id_usuario aponta para a tabela ALUNO (não para usuario): só aluno tem progresso
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Aluno aluno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_exercicio", nullable = false)
    private Exercicio exercicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProgresso status = StatusProgresso.EM_ANDAMENTO;

    @Column(nullable = false)
    private Integer tentativas = 0;

    @Column(name = "pontos_ganhos", nullable = false) // XP que ESTE exercício já deu ao aluno (PR2: no máximo uma vez)
    private Integer pontosGanhos = 0;

    @Column(name = "resposta_json", columnDefinition = "json") // última resposta enviada (PR4)
    private String respostaJson;

    // preenchida e atualizada pelo próprio MySQL (DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)
    @Column(name = "data_resposta", insertable = false, updatable = false)
    private LocalDateTime dataResposta;

    /** PR7: acertar depois de ter errado vale metade do XP. */
    static final int DIVISOR_XP_APOS_ERRO = 2;

    public ProgressoExercicio() {}

    public ProgressoExercicio(Aluno aluno, Exercicio exercicio) {
        this.aluno = aluno;
        this.exercicio = exercicio;
    }

    public Integer getId() { return id; }

    public Aluno getAluno() { return aluno; }
    public Exercicio getExercicio() { return exercicio; }
    // sem setters de aluno/exercicio: a linha é SEMPRE daquele aluno com aquele exercício (UNIQUE)

    /**
     * Registra uma resposta do aluno a este exercício, aplicando as regras de negócio:
     *  PR3: toda resposta enviada conta +1 tentativa (mesmo depois de concluído);
     *  PR4: guarda a última resposta enviada;
     *  PR1: se acertou, o status vira CONCLUIDO e nunca mais volta para EM_ANDAMENTO;
     *  PR2: o XP do exercício só é ganho na PRIMEIRA vez que ele é concluído;
     *  PR7: se o aluno ERROU antes desse primeiro acerto, ganha só METADE do XP
     *       (arredondada para baixo), porque depois de errar ele já viu a resposta certa (T1).
     *
     * @param respostaJson resposta enviada pelo aluno (JSON, obrigatória)
     * @param acertou      resultado da correção feita pelo backend
     * @return XP ganho NESTA resposta (0 se errou ou se o exercício já estava concluído);
     *         o Service usa esse valor para somar na pontuação do aluno.
     */
    public int registrarResposta(String respostaJson, boolean acertou) {
        Objects.requireNonNull(respostaJson, "A resposta não pode ser nula");

        // enquanto não está concluído, qualquer tentativa anterior foi um ERRO
        boolean errouAntes = this.tentativas > 0;

        this.tentativas++;                  // PR3
        this.respostaJson = respostaJson;   // PR4

        boolean primeiraConclusao = acertou && this.status != StatusProgresso.CONCLUIDO;
        if (primeiraConclusao) {            // PR1 + PR2
            this.status = StatusProgresso.CONCLUIDO;
            this.pontosGanhos = errouAntes
                    ? exercicio.getPontos() / DIVISOR_XP_APOS_ERRO // PR7: divisão inteira (15 -> 7)
                    : exercicio.getPontos();
            return this.pontosGanhos;
        }
        return 0; // errou, ou acertou de novo algo já concluído (sem XP repetido)
    }

    public boolean isConcluido() {
        return this.status == StatusProgresso.CONCLUIDO;
    }

    // apenas getters: o estado só muda via registrarResposta()
    public StatusProgresso getStatus() { return status; }
    public Integer getTentativas() { return tentativas; }
    public Integer getPontosGanhos() { return pontosGanhos; }
    public String getRespostaJson() { return respostaJson; }

    public LocalDateTime getDataResposta() { return dataResposta; } // sem set: quem cuida é o banco
}
