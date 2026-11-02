package br.com.logicup.backend.dto;

/**
 * Correção de UM exercício (T1: feedback completo).
 * respostaCorreta: formato próprio para exibição (alternativa, lista, pares, texto) — nunca o gabarito cru.
 *
 * Sprint 9 (ADR-001), campos NOVOS (os antigos não mudaram, então o frontend atual continua funcionando):
 *  pendente: true quando a questão de código não pôde ser executada (executor fora do ar).
 *            Nesse caso: correta=false, xpGanho=0, e explicacao/respostaCorreta vêm NULL. Mostrar a resposta
 *            modelo numa questão pendente permitiria copiar e reenviar com XP cheio (a pendência não conta erro).
 *  execucao: detalhe da execução (casos de teste, erro de compilação, dica). Só nas questões CODIGO; nas outras, null.
 */
public record ResultadoExercicioResponse(Integer exercicioId, boolean correta, int xpGanho,
                                         String explicacao, Object respostaCorreta,
                                         boolean pendente, DetalheCorrecaoResponse execucao) {

    public static ResultadoExercicioResponse pendente(Integer exercicioId, DetalheCorrecaoResponse execucao) {
        return new ResultadoExercicioResponse(exercicioId, false, 0, null, null, true, execucao);
    }
}
