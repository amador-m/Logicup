package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;

/**
 * Padrão STRATEGY: cada formato de resposta tem sua própria "estratégia" de correção.
 * O CorrecaoService escolhe a estratégia certa pelo formato do exercício.
 * Formato novo (ex.: BLOCOS) = uma classe nova que implementa esta interface, sem mexer nas outras.
 */
public interface Corretor {

    /** Qual formato esta estratégia corrige. */
    FormatoResposta formato();

    /**
     * @param resposta o que o aluno enviou (objeto JSON já convertido em Map pelo Spring)
     * @return true se a resposta está correta
     */
    boolean corrigir(Exercicio exercicio, Object resposta);

    /**
     * A resposta certa PARA MOSTRAR ao aluno depois da correção (T1).
     * Nunca devolve o gabarito cru (ex.: TEXTO_LIVRE não expõe as palavras-chave).
     */
    Object respostaCorreta(Exercicio exercicio);
}
