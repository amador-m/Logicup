import { useState } from "react";
import FeedbackQuestao from "./FeedbackQuestao";
import { ROTULO_TIPO } from "../../rotulos";
import { ExerciseItem, ExerciseMeta, HintButton, HintBox } from "../../styles";
import CodigoJava from "../../../../components/ui/CodigoJava";

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

            {/* Sprint 9: código com cara de IDE (cores, números de linha) e espaço antes da área de resposta.
                Fluxograma em texto (tem "→", ex.: F1.Q8) não é Java: aparece sem cores. */}
            {exercicio.codigoApoio && (
                <CodigoJava
                    codigo={exercicio.codigoApoio}
                    variante={exercicio.codigoApoio.includes("→") ? "texto" : "java"}
                    titulo={exercicio.codigoApoio.includes("→") ? "Fluxograma" : undefined}
                />
            )}

            {children}

            {resultado && <FeedbackQuestao exercicio={exercicio} resultado={resultado} />}

            {exercicio.dicas.slice(0, dicasVisiveis).map((dica) => (
                <HintBox key={dica}>💡 {dica}</HintBox>
            ))}

            {temMaisDicas && (
                <HintButton type="button" onClick={() => setDicasVisiveis((n) => n + 1)}>
                    <span className="lampada" aria-hidden="true">💡</span>
                    {dicasVisiveis === 0 ? "Ver dica" : "Ver outra dica"}
                    {/* com mais de uma dica, o aluno sabe quantas ainda tem */}
                    {exercicio.dicas.length > 1 && (
                        <span className="contador">({dicasVisiveis + 1} de {exercicio.dicas.length})</span>
                    )}
                </HintButton>
            )}
        </ExerciseItem>
    );
};

export default ExercicioCard;
