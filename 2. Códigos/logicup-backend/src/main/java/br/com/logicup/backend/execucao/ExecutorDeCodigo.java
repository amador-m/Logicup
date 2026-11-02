package br.com.logicup.backend.execucao;

import br.com.logicup.backend.exception.ExecutorIndisponivelException;

/**
 * Sprint 9 (ADR-001): o CONTRATO de quem executa código Java de forma isolada.
 *
 * Por que uma interface: o resto do LogiCup (correção, botão Executar, Blockly) depende só deste
 * contrato, e não do Piston. Isso é o "D" do SOLID (inversão de dependência):
 *   - em produção, o Spring entrega um ExecutorPiston;
 *   - com o executor desligado na configuração, um ExecutorDesligado;
 *   - nos testes JUnit, um executor falso, sem Docker e sem rede.
 * Trocar o Piston por outro executor no futuro = escrever outra classe que implemente esta interface.
 */
public interface ExecutorDeCodigo {

    /**
     * Executa o programa e devolve como ele terminou.
     * Erros do PROGRAMA (não compila, exceção, laço infinito) NÃO são exceções aqui: vêm no
     * ResultadoExecucao, porque são uma resposta normal para o aluno.
     *
     * @throws ExecutorIndisponivelException se não foi possível executar (executor fora do ar,
     *         ocupado demais ou desligado). Quem chama decide o que fazer (ex.: reserva por palavras-chave).
     */
    ResultadoExecucao executar(ProgramaJava programa);
}
