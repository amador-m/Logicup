package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.dto.DetalheCorrecaoResponse;

/**
 * Sprint 9 (ADR-001): o resultado da correção de UMA questão.
 *
 * Antes, corrigir devolvia só true/false. A questão de código precisa de um terceiro estado:
 *   CORRETA   -> registra o acerto e dá XP
 *   INCORRETA -> registra a tentativa errada
 *   PENDENTE  -> NÃO registra nada (decisão 1-A de 02/11): o executor estava fora do ar, então não
 *                sabemos se está certa. Não dá XP e não conta como erro; o aluno reenvia depois.
 * detalhe: explicação para o aluno (só as questões de código preenchem; nas outras é null).
 */
public record ResultadoCorrecao(Situacao situacao, DetalheCorrecaoResponse detalhe) {

    public enum Situacao { CORRETA, INCORRETA, PENDENTE }

    public static ResultadoCorrecao de(boolean correta) {
        return new ResultadoCorrecao(correta ? Situacao.CORRETA : Situacao.INCORRETA, null);
    }

    public boolean correta() {
        return situacao == Situacao.CORRETA;
    }

    public boolean pendente() {
        return situacao == Situacao.PENDENTE;
    }
}
