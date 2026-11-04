import { QuestionFeedback } from "../../styles";
import CodigoJava from "../../../../components/ui/CodigoJava";
import { ProgressoCasos, CasoQueFalhou, Dica } from "./estilosCodigo";

/*
 * Resultado de UMA questão depois do envio (T1): acertou/errou, XP, explicação e a resposta certa.
 * A "respostaCorreta" vem do backend já em formato de EXIBIÇÃO (nunca o gabarito cru):
 *   ESCOLHA_UNICA  -> {id, texto}
 *   ORDENACAO      -> [{id, texto}, ...] na ordem certa
 *   ASSOCIACAO     -> [{item, opcao}, ...]
 *   RESPOSTA_CURTA -> "texto"  ou, com várias lacunas (Sprint 9), ["int", "18"]
 *   TEXTO_LIVRE    -> "resposta modelo" (as palavras-chave nunca saem do servidor)
 *   CODIGO         -> "resposta modelo" (código; os casos de teste nunca saem do servidor)
 *   BLOCOS         -> o Java do programa montado certo (mesma correção do CODIGO)
 */
function RespostaCorreta({ formato, valor }) {
    switch (formato) {
        case "ESCOLHA_UNICA":
            return <p>Resposta correta: <strong>{valor.texto}</strong></p>;
        case "ORDENACAO":
            return (
                <>
                    <p>Ordem correta:</p>
                    <ol>
                        {valor.map((item) => (
                            <li key={item.id}>{item.texto}</li>
                        ))}
                    </ol>
                </>
            );
        case "ASSOCIACAO":
            return (
                <>
                    <p>Associações corretas:</p>
                    <ul>
                        {valor.map((par) => (
                            <li key={par.item}>
                                {par.item} → <strong>{par.opcao}</strong>
                            </li>
                        ))}
                    </ul>
                </>
            );
        case "RESPOSTA_CURTA":
            if (Array.isArray(valor)) {
                return (
                    <p>
                        Resposta correta:{" "}
                        {valor.map((lacuna, indice) => (
                            <span key={indice}>
                                {indice > 0 && " · "}
                                {indice + 1}ª lacuna: <strong>{lacuna}</strong>
                            </span>
                        ))}
                    </p>
                );
            }
            return <p>Resposta correta: <strong>{valor}</strong></p>;
        case "TEXTO_LIVRE":
            return <p>Exemplo de resposta: <em>{valor}</em></p>;
        case "CODIGO":
            return (
                <>
                    <p>Uma solução possível:</p>
                    <CodigoJava codigo={valor} titulo="Resposta modelo" />
                </>
            );
        case "BLOCOS": // Sprint 9 (Blockly): o Java do programa montado na ordem certa
            return (
                <>
                    <p>Montado na ordem certa, os blocos formam este Java:</p>
                    <CodigoJava codigo={valor} titulo="Resposta modelo" />
                </>
            );
        default:
            return <p>Resposta correta: <strong>{String(valor)}</strong></p>;
    }
}

/*
 * Sprint 9 (ADR-001): o "porquê" da correção de uma questão de CÓDIGO (resultado.execucao).
 *   - quantos casos de teste passaram;
 *   - SÓ o primeiro caso que falhou (esperado x obtido); os demais continuam ocultos;
 *   - a mensagem do Java (com a linha do código do aluno) e uma dica em português.
 */
function DetalheExecucao({ execucao }) {
    const temProgresso = execucao.totalCasos != null && execucao.casosAprovados != null;
    const percentual = temProgresso ? Math.round((execucao.casosAprovados / execucao.totalCasos) * 100) : 0;
    return (
        <>
            <p>{execucao.mensagem}</p>
            {temProgresso && (
                <ProgressoCasos>
                    <span className="texto">
                        {execucao.casosAprovados} de {execucao.totalCasos} casos de teste
                    </span>
                    <div className="trilho" role="img" aria-label={`${execucao.casosAprovados} de ${execucao.totalCasos} casos de teste passaram`}>
                        <div className="preenchido" style={{ width: `${percentual}%` }} />
                    </div>
                </ProgressoCasos>
            )}
            {execucao.caso && (
                <CasoQueFalhou>
                    Caso de teste: <code>{execucao.caso}</code>
                </CasoQueFalhou>
            )}
            {execucao.esperado != null && <CodigoJava codigo={execucao.esperado} variante="console" titulo="Era esperado" />}
            {execucao.obtido != null && (
                <CodigoJava codigo={execucao.obtido || "(o programa não exibiu nada)"} variante="console" titulo="Seu programa exibiu" />
            )}
            {execucao.erros && <CodigoJava codigo={execucao.erros} variante="texto" titulo="Mensagem do Java" />}
            {execucao.dica && <Dica>💡 {execucao.dica}</Dica>}
        </>
    );
}

/* aviso: texto opcional no fim do feedback (ex.: "Você poderá refazer esta questão no fim da rodada."). */
const FeedbackQuestao = ({ exercicio, resultado, aviso }) => {
    // Sprint 9 (decisão 1-A): PENDENTE = não deu para executar agora. Não é acerto nem erro.
    if (resultado.pendente) {
        return (
            <QuestionFeedback className="pending" role="status">
                <strong className="verdict">⏳ Ainda não validado</strong>
                {resultado.execucao && <p>{resultado.execucao.mensagem}</p>}
                {aviso && <p className="retry">{aviso}</p>}
            </QuestionFeedback>
        );
    }
    return (
    <QuestionFeedback className={resultado.correta ? "success" : "error"} role="status">
        <strong className="verdict">
            {resultado.correta ? "✓ Correto!" : "✗ Não foi dessa vez"}
            {resultado.xpGanho > 0 && <span> +{resultado.xpGanho} XP</span>}
        </strong>

        {/* PR7: acerto depois de um erro vale metade (o cálculo é do backend; aqui só explicamos) */}
        {resultado.xpGanho > 0 && resultado.xpGanho < exercicio.pontos && (
            <p className="note">Metade do XP, porque você errou esta questão antes. Na próxima, tente de primeira!</p>
        )}

        {/* Sprint 9: questão de código -> casos de teste, erro e dica ANTES da explicação */}
        {resultado.execucao && <DetalheExecucao execucao={resultado.execucao} />}

        {/* T1: com acerto ou erro, sempre mostra a explicação e a resposta certa */}
        <p>{resultado.explicacao}</p>
        {resultado.respostaCorreta != null && (
            <RespostaCorreta formato={exercicio.formato} valor={resultado.respostaCorreta} />
        )}

        {aviso && <p className="retry">{aviso}</p>}
    </QuestionFeedback>
    );
};

export default FeedbackQuestao;
