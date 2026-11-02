package br.com.logicup.backend.execucao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Sprint 9 (ADR-001): executor FALSO, só para os testes JUnit.
 * Não roda nada: devolve o que o teste mandar (comportamento) e guarda os programas recebidos,
 * para o teste conferir o que foi enviado. É um "dublê de teste" (test double), do tipo "fake".
 *
 * É isto que permite testar a correção e o botão Executar sem Docker, sem rede e em milissegundos.
 */
public class ExecutorFalso implements ExecutorDeCodigo {

    private Function<ProgramaJava, ResultadoExecucao> comportamento =
            programa -> new ResultadoExecucao(SituacaoExecucao.SUCESSO, "", "", 0, 1L);
    private final List<ProgramaJava> recebidos = new ArrayList<>();

    public void quandoExecutar(Function<ProgramaJava, ResultadoExecucao> comportamento) {
        this.comportamento = comportamento;
    }

    public List<ProgramaJava> recebidos() {
        return recebidos;
    }

    @Override
    public ResultadoExecucao executar(ProgramaJava programa) {
        recebidos.add(programa);
        return comportamento.apply(programa);
    }
}
