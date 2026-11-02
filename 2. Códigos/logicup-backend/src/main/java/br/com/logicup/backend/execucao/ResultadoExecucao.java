package br.com.logicup.backend.execucao;

/**
 * Sprint 9 (ADR-001): o resultado de UMA execução.
 *
 * saida:       o que o programa escreveu com System.out (stdout)
 * erros:       mensagens de erro (stderr): do compilador ou da exceção
 * codigoSaida: código de saída do processo (0 = normal); null se o processo foi morto
 * tempoMs:     tempo de relógio gasto, em milissegundos (null se o executor não informar)
 */
public record ResultadoExecucao(SituacaoExecucao situacao, String saida, String erros,
                                Integer codigoSaida, Long tempoMs) {

    public ResultadoExecucao {
        saida = saida == null ? "" : saida;
        erros = erros == null ? "" : erros;
    }

    public boolean sucesso() {
        return situacao == SituacaoExecucao.SUCESSO;
    }
}
