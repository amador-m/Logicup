package br.com.logicup.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "aluno")
@PrimaryKeyJoinColumn(name = "id_usuario")
// em sql: id_usuario INT PRIMARY KEY, FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
public class Aluno extends Usuario { // subclasse que herda o construtor do usuário

    /*
     * R1-bis: os dados de gamificação do aluno NÃO têm setters.
     * Eles só mudam pelos métodos adicionarPontos(), registrarAtividade() e atualizarNivel(),
     * que garantem as regras próprias do aluno. Quem decide QUANDO chamar cada um
     * (e com quais valores) é a camada de Service.
     */

    @Column(name = "nivel_atual", nullable = false) // N-A: nível = fase mais alta liberada
    private Integer nivelAtual = 1;

    @Column(name = "pontuacao_acumulada", nullable = false) // XP total (conceito separado do nível)
    private Integer pontuacaoAcumulada = 0;

    @Column(name = "streak_atual", nullable = false) // dias seguidos com atividade
    private Integer streakAtual = 0;

    @Column(name = "ultima_atividade_data")
    private LocalDate ultimaAtividadeData;

    public Aluno() {}

    public Aluno(String nome, String email, String senha) {
        super(nome, email, senha, TipoUsuario.ALUNO); // chama o construor da classe pai pra preencher os campos herdados
    }

    /**
     * Soma XP à pontuação do aluno. A pontuação só cresce: XP negativo é recusado.
     * @param xp pontos a somar (0 é permitido — ex.: resposta errada)
     */
    public void adicionarPontos(int xp) {
        if (xp < 0) {
            throw new IllegalArgumentException("XP não pode ser negativo: " + xp);
        }
        this.pontuacaoAcumulada = valorOuPadrao(pontuacaoAcumulada, 0) + xp;
    }

    /**
     * Registra que o aluno teve atividade na data informada e atualiza o streak.
     * A data vem de fora (o Service decide o "hoje" no fuso correto).
     *
     *  - primeira atividade        -> streak = 1
     *  - mesmo dia da última       -> nada muda
     *  - dia seguinte à última     -> streak + 1
     *  - 2 ou mais dias depois     -> streak recomeça em 1
     *  - data ANTERIOR à última    -> ignorada (relógio inconsistente; não mexe em nada)
     */
    public void registrarAtividade(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("A data da atividade é obrigatória");
        }

        if (ultimaAtividadeData == null) {
            this.streakAtual = 1;
        } else if (data.isBefore(ultimaAtividadeData) || data.isEqual(ultimaAtividadeData)) {
            return; // data antiga ou mesmo dia: streak e data permanecem iguais
        } else if (data.isEqual(ultimaAtividadeData.plusDays(1))) {
            this.streakAtual = valorOuPadrao(streakAtual, 0) + 1;
        } else {
            this.streakAtual = 1; // pulou pelo menos um dia: a sequência quebrou
        }
        this.ultimaAtividadeData = data;
    }

    /**
     * Atualiza o nível (N-A: fase mais alta liberada). Quem CALCULA a fase é o Service;
     * aqui só garantimos que o valor é válido e que o nível nunca diminui.
     */
    public void atualizarNivel(int faseMaisAltaLiberada) {
        if (faseMaisAltaLiberada < 1) {
            throw new IllegalArgumentException("Nível deve ser no mínimo 1: " + faseMaisAltaLiberada);
        }
        this.nivelAtual = Math.max(valorOuPadrao(nivelAtual, 1), faseMaisAltaLiberada);
    }

    /**
     * Streak para EXIBIR hoje. O valor gravado só é recalculado na próxima atividade;
     * se o aluno já pulou um dia, a sequência está quebrada e deve aparecer como 0 (como no Duolingo).
     */
    public int getStreakVigente(LocalDate hoje) {
        if (ultimaAtividadeData == null) {
            return 0;
        }
        boolean sequenciaAtiva = !ultimaAtividadeData.isBefore(hoje.minusDays(1)); // hoje ou ontem
        return sequenciaAtiva ? valorOuPadrao(streakAtual, 0) : 0;
    }

    // proteção extra caso algum valor venha nulo (o banco já impede isso com NOT NULL - A3)
    private static int valorOuPadrao(Integer valor, int padrao) {
        return valor != null ? valor : padrao;
    }

    // apenas getters
    public Integer getNivelAtual() { return nivelAtual; }
    public Integer getPontuacaoAcumulada() { return pontuacaoAcumulada; }
    public Integer getStreakAtual() { return streakAtual; }
    public LocalDate getUltimaAtividadeData() { return ultimaAtividadeData; }
}
