package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.model.Exercicio;
import br.com.logicup.backend.model.FormatoResposta;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Escolhe o corretor (estratégia) pelo formato do exercício.
 * O Spring injeta a LISTA de todos os beans que implementam Corretor:
 * adicionar um formato novo não exige mudar esta classe.
 */
@Service
public class CorrecaoService {

    private final Map<FormatoResposta, Corretor> corretores = new EnumMap<>(FormatoResposta.class);

    public CorrecaoService(List<Corretor> todosOsCorretores) {
        todosOsCorretores.forEach(corretor -> corretores.put(corretor.formato(), corretor));
    }

    public boolean corrigir(Exercicio exercicio, Object resposta) {
        return corretorPara(exercicio).corrigir(exercicio, resposta);
    }

    public Object respostaCorreta(Exercicio exercicio) {
        return corretorPara(exercicio).respostaCorreta(exercicio);
    }

    private Corretor corretorPara(Exercicio exercicio) {
        Corretor corretor = corretores.get(exercicio.getFormatoResposta());
        if (corretor == null) { // hoje: BLOCOS (X4)
            throw new RequisicaoInvalidaException(
                    "O formato " + exercicio.getFormatoResposta() + " ainda não é suportado.");
        }
        return corretor;
    }
}
