package br.com.logicup.backend.execucao;

import java.time.Duration;

/**
 * Sprint 9 (ADR-001): as configurações do executor, lidas do application.properties (logicup.executor.*).
 * Ficam num record para o ExecutorPiston não depender do Spring (dá para criá-lo num teste com "new").
 *
 * url:             endereço do Piston (só na rede interna; NUNCA exposto na internet)
 * versaoJava:      versão instalada no Piston (hoje só existe a 15.0.2)
 * tempoExecucao:   tempo máximo de relógio de cada execução (inclui a compilação, no modo arquivo único)
 * tempoCpu:        tempo máximo de CPU
 * memoriaBytes:    memória máxima (o padrão do Piston é ILIMITADO, por isso sempre mandamos um valor)
 * simultaneas:     quantas execuções podem acontecer ao mesmo tempo (as outras esperam na fila)
 * esperaNaFila:    quanto tempo uma requisição espera a sua vez antes de desistir
 */
public record ParametrosExecutor(String url, String versaoJava, Duration tempoExecucao, Duration tempoCpu,
                                 long memoriaBytes, int simultaneas, Duration esperaNaFila) {

    public ParametrosExecutor {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("logicup.executor.url é obrigatório");
        if (tempoExecucao.isNegative() || tempoExecucao.isZero()) throw new IllegalArgumentException("tempo de execução inválido");
        if (tempoCpu.isNegative() || tempoCpu.isZero()) throw new IllegalArgumentException("tempo de CPU inválido");
        if (memoriaBytes < 32L * 1024 * 1024) throw new IllegalArgumentException("memória mínima: 32 MB (a JVM não sobe com menos)");
        if (simultaneas < 1) throw new IllegalArgumentException("simultaneas precisa ser >= 1");
        if (esperaNaFila.isNegative()) throw new IllegalArgumentException("espera na fila inválida");
    }
}
