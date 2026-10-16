import { useEffect, useRef } from "react";
import ExercicioCard from "./exercicios/ExercicioCard";
import CampoResposta from "./exercicios/CampoResposta";
import FeedbackQuestao from "./exercicios/FeedbackQuestao";
import MascoteReacao from "./MascoteReacao";
import { estaRespondida } from "../respostas";
import {
    BarraRodada,
    PontoQuestao,
    FeedbackComMascote,
    BottomActions,
    PrimaryButton,
    OutlineButton,
    StatusMessage,
} from "../styles";

const ROTULO_ESTADO = { correta: "concluída", errada: "errada", pendente: "não respondida" };

/** O que o mascote fala ANTES da resposta e DEPOIS da correção. */
function falaDoMascote(exercicio, resultado) {
    if (resultado) {
        return resultado.correta ? "Mandou bem!" : "Quase! Leia a explicação, ela ajuda.";
    }
    return exercicio.tipo === "BOSS_CHALLENGE" ? "Hora do Boss! Capricha." : "Leia com calma e responda.";
}

/*
 * UMA questão por vez (D-S7-1). Componente de APRESENTAÇÃO: todo o estado (fila, respostas,
 * resultados) fica no PainelRodada, para não se perder quando o aluno vai "Rever teoria".
 *
 * passagem = { tipo: "normal" | "refazer" | "revisar", fila: [ids na ordem], indice }
 *   normal/refazer -> responde e recebe feedback na hora;  revisar -> só leitura (professor ou rodada já feita)
 */
const FluxoQuestoes = ({
    exercicios,          // todos os exercícios da rodada, na ordem do banco
    passagem,
    estados,             // { [id]: "pendente" | "errada" | "correta" }
    respostas,           // { [id]: resposta no formato da API }
    resultados,          // { [id]: ResultadoExercicioResponse } desta passagem
    rodadaResumo,        // da trilha: xpGanho e xpTotal REAIS da rodada
    enviando,
    erroEnvio,
    aoResponder,
    aoVerificar,
    aoAvancar,
    aoVoltar,
}) => {
    const feedbackRef = useRef(null);
    const topoRef = useRef(null);

    const porId = new Map(exercicios.map((exercicio) => [exercicio.id, exercicio]));
    const exercicio = porId.get(passagem.fila[passagem.indice]);
    const resultado = resultados[exercicio.id];
    const somenteLeitura = passagem.tipo === "revisar";
    const ultima = passagem.indice === passagem.fila.length - 1;

    // depois da correção, leva o foco ao feedback (teclado e leitor de tela vão direto para ele)
    useEffect(() => {
        if (resultado) {
            feedbackRef.current?.focus();
        }
    }, [resultado]);

    // nova questão: volta para o topo da questão (a barra fixa continua visível)
    useEffect(() => {
        topoRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    }, [exercicio.id]);

    const humor = resultado ? (resultado.correta ? "acerto" : "erro") : "neutro";

    return (
        <>
            <BarraRodada ref={topoRef}>
                <div>
                    <strong>
                        {passagem.tipo === "refazer" && "Refazendo as que errou • "}
                        {passagem.tipo === "revisar" && "Revisão • "}
                        Questão {passagem.indice + 1} de {passagem.fila.length}
                    </strong>
                    {!somenteLeitura && rodadaResumo && (
                        <span>{rodadaResumo.xpGanho} / {rodadaResumo.xpTotal} XP na rodada</span>
                    )}
                </div>

                {/* um ponto por questão da rodada: mostra o que já foi feito, o que errou e onde está */}
                <ol aria-label="Situação das questões da rodada">
                    {exercicios.map((item, posicao) => (
                        <PontoQuestao
                            key={item.id}
                            className={`${estados[item.id]} ${item.id === exercicio.id ? "atual" : ""}`}
                            title={`Questão ${posicao + 1}: ${ROTULO_ESTADO[estados[item.id]]}`}
                        >
                            <span className="sr">{`Questão ${posicao + 1}: ${ROTULO_ESTADO[estados[item.id]]}`}</span>
                        </PontoQuestao>
                    ))}
                </ol>
            </BarraRodada>

            {/* key: cada questão é um card novo (as dicas abertas não "vazam" para a próxima) */}
            <ExercicioCard
                key={exercicio.id}
                exercicio={exercicio}
                numero={exercicios.indexOf(exercicio) + 1}
                concluido={estados[exercicio.id] === "correta"}
            >
                <CampoResposta
                    exercicio={exercicio}
                    valor={respostas[exercicio.id]}
                    aoMudar={(resposta) => aoResponder(exercicio.id, resposta)}
                    desabilitado={somenteLeitura || enviando || Boolean(resultado)}
                />
            </ExercicioCard>

            {!somenteLeitura && (
                <FeedbackComMascote>
                    <MascoteReacao key={`${exercicio.id}-${humor}`} humor={humor} fala={falaDoMascote(exercicio, resultado)} />
                    {resultado && (
                        <div ref={feedbackRef} tabIndex={-1}>
                            <FeedbackQuestao
                                exercicio={exercicio}
                                resultado={resultado}
                                aviso={resultado.correta ? null : "Você poderá refazer esta questão no fim da rodada."}
                            />
                        </div>
                    )}
                </FeedbackComMascote>
            )}

            {erroEnvio && <StatusMessage role="alert" className="error">{erroEnvio}</StatusMessage>}

            <BottomActions>
                {somenteLeitura ? (
                    <>
                        <OutlineButton type="button" disabled={passagem.indice === 0} onClick={aoVoltar}>
                            ← Anterior
                        </OutlineButton>
                        <PrimaryButton type="button" onClick={aoAvancar}>
                            {ultima ? "Encerrar revisão" : "Próxima →"}
                        </PrimaryButton>
                    </>
                ) : resultado ? (
                    <PrimaryButton type="button" onClick={aoAvancar}>
                        {ultima ? "Ver resultado da rodada" : "Continuar →"}
                    </PrimaryButton>
                ) : (
                    <PrimaryButton
                        type="button"
                        disabled={enviando || !estaRespondida(exercicio, respostas[exercicio.id])}
                        onClick={() => aoVerificar(exercicio)}
                    >
                        {enviando ? "Verificando..." : "Verificar"}
                    </PrimaryButton>
                )}
            </BottomActions>
        </>
    );
};

export default FluxoQuestoes;
