package br.com.logicup.backend.execucao;

import br.com.logicup.backend.exception.ExecutorIndisponivelException;

/**
 * Sprint 9 (ADR-001): usado quando logicup.executor.habilitado=false
 * (ex.: no seu PC sem Docker, ou se precisarmos desligar o executor em produção às pressas).
 * O site continua funcionando: o botão Executar avisa que está indisponível, e a correção usa a reserva.
 */
public class ExecutorDesligado implements ExecutorDeCodigo {

    @Override
    public ResultadoExecucao executar(ProgramaJava programa) {
        throw new ExecutorIndisponivelException(
                "A execução de código está temporariamente indisponível. Tente novamente mais tarde.");
    }
}
