import { QuestionFeedback } from "../../styles";

/*
 * Resultado de UMA questão depois do envio (T1): acertou/errou, XP, explicação e a resposta certa.
 * A "respostaCorreta" vem do backend já em formato de EXIBIÇÃO (nunca o gabarito cru):
 *   ESCOLHA_UNICA  -> {id, texto}
 *   ORDENACAO      -> [{id, texto}, ...] na ordem certa
 *   ASSOCIACAO     -> [{item, opcao}, ...]
 *   RESPOSTA_CURTA -> "texto"
 *   TEXTO_LIVRE    -> "resposta modelo" (as palavras-chave nunca saem do servidor)
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
        case "TEXTO_LIVRE":
            return <p>Exemplo de resposta: <em>{valor}</em></p>;
        default:
            return <p>Resposta correta: <strong>{String(valor)}</strong></p>;
    }
}

/* aviso: texto opcional no fim do feedback (ex.: "Você poderá refazer esta questão no fim da rodada."). */
const FeedbackQuestao = ({ exercicio, resultado, aviso }) => (
    <QuestionFeedback className={resultado.correta ? "success" : "error"} role="status">
        <strong className="verdict">
            {resultado.correta ? "✓ Correto!" : "✗ Não foi dessa vez"}
            {resultado.xpGanho > 0 && <span> +{resultado.xpGanho} XP</span>}
        </strong>

        {/* PR7: acerto depois de um erro vale metade (o cálculo é do backend; aqui só explicamos) */}
        {resultado.xpGanho > 0 && resultado.xpGanho < exercicio.pontos && (
            <p className="note">Metade do XP, porque você errou esta questão antes. Na próxima, tente de primeira!</p>
        )}

        {/* T1: com acerto ou erro, sempre mostra a explicação e a resposta certa */}
        <p>{resultado.explicacao}</p>
        {resultado.respostaCorreta != null && (
            <RespostaCorreta formato={exercicio.formato} valor={resultado.respostaCorreta} />
        )}

        {aviso && <p className="retry">{aviso}</p>}
    </QuestionFeedback>
);

export default FeedbackQuestao;
