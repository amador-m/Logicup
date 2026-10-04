import { useState } from "react";
import FeedbackQuestao from "./FeedbackQuestao";
import { ROTULO_TIPO } from "../../rotulos";
import { ExerciseItem, ExerciseMeta, CodeBox, HintButton, HintBox } from "../../styles";

/*
 * Um exercício da rodada. Recebe os dados EXATAMENTE como a API envia (sem gabarito).
 * As dicas aparecem uma por vez, quando o aluno pede.
 * children = área de resposta do formato (CampoResposta).
 * resultado = correção desta questão no último envio (ou undefined).
 * concluido = já acertou alguma vez (vindo da API ou do envio atual).
 */
const ExercicioCard = ({ exercicio, numero, concluido, resultado, children }) => {
    const [dicasVisiveis, setDicasVisiveis] = useState(0);
    const temMaisDicas = dicasVisiveis < exercicio.dicas.length;
    const ehBoss = exercicio.tipo === "BOSS_CHALLENGE";

    return (
        <ExerciseItem className={ehBoss ? "boss" : ""}>
            <ExerciseMeta>
                <span>{ROTULO_TIPO[exercicio.tipo] ?? exercicio.tipo}</span>
                <span>{exercicio.pontos} XP</span>
                {concluido && <span className="done">✓ Concluído</span>}
            </ExerciseMeta>

            <h3>
                {numero}. {exercicio.titulo}
            </h3>
            <p>{exercicio.enunciado}</p>

            {exercicio.codigoApoio && <CodeBox>{exercicio.codigoApoio}</CodeBox>}

            {children}

            {resultado && <FeedbackQuestao exercicio={exercicio} resultado={resultado} />}

            {exercicio.dicas.slice(0, dicasVisiveis).map((dica) => (
                <HintBox key={dica}>💡 {dica}</HintBox>
            ))}

            {temMaisDicas && (
                <HintButton type="button" onClick={() => setDicasVisiveis((n) => n + 1)}>
                    {dicasVisiveis === 0 ? "Ver dica" : "Ver outra dica"}
                </HintButton>
            )}
        </ExerciseItem>
    );
};

export default ExercicioCard;
