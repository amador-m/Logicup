package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import org.springframework.stereotype.Component;

/**
 * Sprint 9 (ADR-001, etapa 1 do Blockly): corrige as questões de BLOCOS.
 *
 * Os blocos viram Java NO NAVEGADOR (geradorJava.js) e chegam aqui como {"codigo": "...", "blocos": {...}}.
 * A partir daí, uma questão de blocos é corrigida EXATAMENTE como uma questão de código: o Java gerado
 * roda no executor com os casos de teste do gabarito (decisão 4-A: qualquer montagem que FUNCIONE vale,
 * não só a do gabarito). O campo "blocos" (o desenho dos blocos) é ignorado na correção.
 *
 * Segurança: o servidor não confia no navegador. Alguém poderia mandar Java digitado em vez de blocos,
 * mas ele passa pela MESMA sandbox e pelos MESMOS testes; não existe atalho para o XP.
 *
 * Por que COMPOSIÇÃO (ter um CorretorCodigo dentro) e não herança (extends CorretorCodigo): com herança
 * existiriam dois beans do tipo CorretorCodigo no Spring, e quem pede um CorretorCodigo (ExecucaoService)
 * receberia um erro de "bean ambíguo". Aqui cada classe tem o seu papel e o CorretorBlocos só delega.
 */
@Component
public class CorretorBlocos implements Corretor {

    private final CorretorCodigo corretorCodigo;

    public CorretorBlocos(CorretorCodigo corretorCodigo) {
        this.corretorCodigo = corretorCodigo;
    }

    @Override
    public FormatoResposta formato() {
        return FormatoResposta.BLOCOS;
    }

    @Override
    public boolean corrigir(Exercicio exercicio, Object resposta) {
        return corretorCodigo.corrigir(exercicio, resposta);
    }

    @Override
    public ResultadoCorrecao avaliar(Exercicio exercicio, Object resposta) {
        return corretorCodigo.avaliar(exercicio, resposta);
    }

    /** O Java do programa montado corretamente (o aluno vê o que os blocos "querem dizer"). */
    @Override
    public Object respostaCorreta(Exercicio exercicio) {
        return corretorCodigo.respostaCorreta(exercicio);
    }
}
