package br.com.logicup.backend.execucao;

/**
 * Sprint 9 (ADR-001): como a execução terminou, em termos que o LogiCup entende.
 * O ExecutorPiston traduz os códigos do Piston (TO, OL, SG...) para esta lista. Assim, o resto do
 * sistema não depende do Piston: se um dia trocarmos de executor, só a tradução muda.
 */
public enum SituacaoExecucao {
    SUCESSO,            // terminou normalmente (código de saída 0)
    ERRO_COMPILACAO,    // o código nem chegou a rodar (ex.: falta ";")
    ERRO_EXECUCAO,      // compilou, mas parou com erro (ex.: ArrayIndexOutOfBoundsException)
    TEMPO_ESGOTADO,     // passou do tempo (ex.: laço infinito)
    SAIDA_EXCEDIDA,     // escreveu texto demais (ex.: println dentro de um laço infinito)
    INTERROMPIDO        // encerrado pelo sistema (ex.: memória ou processos acima do limite)
}
