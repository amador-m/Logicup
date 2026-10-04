package br.com.logicup.backend.service.correcao;

import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Código comum aos corretores: converter JSON em records Java.
 * Usamos records tipados (ex.: Gabarito(String correta)) em vez de navegar no JSON "na mão":
 * o código fica legível e o compilador ajuda a achar erros.
 */
abstract class CorretorBase implements Corretor {

    protected final JsonMapper jsonMapper;

    protected CorretorBase(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /** Lê um JSON guardado no BANCO (gabarito/alternativas). Se estiver errado, é erro de conteúdo nosso -> 500. */
    protected <T> T lerDoBanco(String json, Class<T> tipo, String codigoQuestao) {
        try {
            return jsonMapper.readValue(json, tipo);
        } catch (RuntimeException e) {
            throw new IllegalStateException("JSON inválido no banco para a questão " + codigoQuestao, e);
        }
    }

    /** Converte a resposta ENVIADA PELO ALUNO. Se não tiver o formato esperado -> 400. */
    protected <T> T lerResposta(Object resposta, Class<T> tipo) {
        try {
            return jsonMapper.convertValue(resposta, tipo);
        } catch (RuntimeException e) {
            throw new RequisicaoInvalidaException("Resposta em formato inválido para este tipo de questão.");
        }
    }

    protected void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RequisicaoInvalidaException(mensagem);
        }
    }
}
