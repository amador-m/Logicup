package br.com.logicup.backend.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sprint 9: limitador de tentativas ("rate limiting") por CHAVE, em memória.
 *
 * Como funciona (janela fixa que começa no 1º erro):
 *  - cada chave (ex.: "ana@x.com|203.0.113.7") tem um contador de erros e o instante do 1º erro;
 *  - enquanto a janela (ex.: 15 min) não acabar, cada erro soma 1;
 *  - ao chegar no máximo (ex.: 5), a chave fica BLOQUEADA até a janela acabar;
 *  - quando a janela acaba, o contador some e tudo recomeça do zero.
 *
 * Por que ConcurrentHashMap e compute(): o servidor atende várias requisições ao MESMO tempo (threads).
 * compute() faz "ler o contador + somar 1 + gravar" de forma atômica para cada chave; com um HashMap
 * comum, duas requisições simultâneas poderiam ler o mesmo valor e uma das somas se perderia.
 *
 * Por que Clock: o "agora" vem de um relógio injetado. Nos testes usamos um relógio controlado
 * e "avançamos 15 minutos" sem precisar esperar de verdade.
 *
 * Limitação conhecida (decisão D3): fica em memória — os contadores somem se o servidor reiniciar e
 * não são compartilhados entre várias instâncias. Para o LogiCup (um servidor só) é suficiente.
 *
 * Classe sem Spring (Java puro) de propósito: é a regra, testada por testes de unidade simples.
 */
public class LimitadorTentativas {

    /** Acima disso, a cada novo erro aproveitamos para apagar janelas vencidas (a memória não cresce sem fim). */
    static final int LIMPAR_ACIMA_DE = 10_000;

    private record Janela(Instant inicio, int erros) {}

    private final ConcurrentHashMap<String, Janela> janelas = new ConcurrentHashMap<>();
    private final int maximo;
    private final Duration duracao;
    private final Clock clock;

    public LimitadorTentativas(int maximo, Duration duracao, Clock clock) {
        if (maximo < 1 || duracao.isNegative() || duracao.isZero()) {
            throw new IllegalArgumentException("Limite inválido: máximo >= 1 e janela > 0");
        }
        this.maximo = maximo;
        this.duracao = duracao;
        this.clock = clock;
    }

    /**
     * Quanto tempo falta para a chave poder tentar de novo.
     * Vazio = pode tentar agora (não chegou ao máximo, ou a janela já acabou).
     */
    public Optional<Duration> bloqueioRestante(String chave) {
        Janela janela = janelas.get(chave);
        if (janela == null || janela.erros() < maximo) {
            return Optional.empty();
        }
        Duration restante = Duration.between(clock.instant(), fimDa(janela));
        return restante.isNegative() || restante.isZero() ? Optional.empty() : Optional.of(restante);
    }

    /**
     * Sprint 10: CONFERE e CONTA numa operação só (atômica).
     *
     * Por que existe: o jeito antigo era "bloqueioRestante()" antes de conferir a senha e "registrarErro()"
     * depois. Entre os dois há o BCrypt (~0,1 s). Vinte tentativas disparadas AO MESMO TEMPO passavam todas
     * pela conferência antes de qualquer uma ser contada: no teste de capacidade, 13 de 20 passaram com
     * limite 5. Isso é uma CONDIÇÃO DE CORRIDA ("check-then-act"): duas etapas que deveriam ser uma só.
     *
     * Aqui, o ConcurrentHashMap.compute() roda a função com a chave TRAVADA: enquanto uma thread decide e
     * conta, as outras esperam. Então, com limite 5, exatamente 5 passam, por mais simultâneas que sejam.
     *
     * @return vazio = pode seguir (e a tentativa JÁ foi contada); com valor = bloqueado, e quanto falta
     */
    public Optional<Duration> tentar(String chave) {
        Instant agora = clock.instant();
        Duration[] bloqueio = new Duration[1]; // o compute devolve a Janela; o bloqueio sai por aqui
        janelas.compute(chave, (k, atual) -> {
            if (atual == null || !agora.isBefore(fimDa(atual))) {
                return new Janela(agora, 1);                 // janela nova: 1ª tentativa
            }
            if (atual.erros() >= maximo) {
                bloqueio[0] = Duration.between(agora, fimDa(atual));
                return atual;                                // bloqueado: não conta de novo
            }
            return new Janela(atual.inicio(), atual.erros() + 1);
        });
        if (janelas.size() > LIMPAR_ACIMA_DE) {
            removerVencidas();
        }
        return Optional.ofNullable(bloqueio[0]);
    }

    /**
     * Sprint 10: devolve UMA tentativa contada pelo tentar(), quando ela acabou não sendo um erro
     * (ex.: a senha estava certa; o código de turma existia). Nunca fica abaixo de zero.
     */
    public void estornar(String chave) {
        janelas.computeIfPresent(chave, (k, atual) ->
                atual.erros() <= 1 ? null : new Janela(atual.inicio(), atual.erros() - 1));
    }

    /** Conta mais um erro para a chave (abre uma janela nova se não houver uma ainda valendo). */
    public void registrarErro(String chave) {
        Instant agora = clock.instant();
        janelas.compute(chave, (k, atual) ->
                (atual == null || !agora.isBefore(fimDa(atual)))
                        ? new Janela(agora, 1)
                        : new Janela(atual.inicio(), atual.erros() + 1));
        if (janelas.size() > LIMPAR_ACIMA_DE) {
            removerVencidas();
        }
    }

    /** Apaga o contador da chave (ex.: login certo zera os erros daquela conta naquele IP). */
    public void limpar(String chave) {
        janelas.remove(chave);
    }

    /** Remove janelas que já acabaram. */
    void removerVencidas() {
        Instant agora = clock.instant();
        janelas.entrySet().removeIf(entrada -> !agora.isBefore(fimDa(entrada.getValue())));
    }

    /** Só para os testes de unidade: quantas chaves estão sendo acompanhadas. */
    int chavesAcompanhadas() {
        return janelas.size();
    }

    private Instant fimDa(Janela janela) {
        return janela.inicio().plus(duracao);
    }
}
