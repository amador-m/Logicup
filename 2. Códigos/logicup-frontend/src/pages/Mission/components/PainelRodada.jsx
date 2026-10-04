import { useEffect, useRef, useState } from "react";
import { trilhaApi } from "../../../services/api";
import { estaRespondida } from "../respostas";
import TeoriaRodada from "./TeoriaRodada";
import ExercicioCard from "./exercicios/ExercicioCard";
import CampoResposta from "./exercicios/CampoResposta";
import ResultadoRodada from "./ResultadoRodada";
import {
    Card,
    CardHeader,
    ExerciseList,
    BottomActions,
    PrimaryButton,
    OutlineButton,
    StatusMessage,
    SubmitInfo,
} from "../styles";

/*
 * Conteúdo de UMA rodada: busca GET /api/rodadas/{id}, alterna teoria/exercícios
 * e envia as respostas (POST /api/rodadas/{id}/respostas).
 * A página cria este componente com key={rodadaId}: ao trocar de aba o React monta um painel
 * NOVO, então carregamento, etapa, respostas e resultados começam do zero sem código extra.
 *
 * Props vindas da página:
 *   aoEnviar()              -> avisa que houve envio (a página atualiza a trilha: abas, progresso)
 *   localizarRodada(id)     -> {id, ordem, faseOrdem, mesmaFase} de uma rodada da trilha
 *   aoIrParaRodada(info)    -> navega para a rodada liberada
 */
const PainelRodada = ({ rodadaId, somenteLeitura, aoEnviar, localizarRodada, aoIrParaRodada }) => {
    const [rodada, setRodada] = useState(null);
    const [erro, setErro] = useState(null);
    const [etapa, setEtapa] = useState("teoria"); // "teoria" | "exercicios"
    // respostas no MESMO formato que a API espera: { [exercicioId]: {"alternativa":"a"} | {"texto":"..."} | ... }
    const [respostas, setRespostas] = useState({});
    const [resultados, setResultados] = useState({}); // { [exercicioId]: ResultadoExercicioResponse }
    const [resumo, setResumo] = useState(null);         // ResumoEnvioResponse do último envio
    const [enviando, setEnviando] = useState(false);
    const [erroEnvio, setErroEnvio] = useState(null);
    const resumoRef = useRef(null);

    useEffect(() => {
        let ativo = true;
        trilhaApi.rodada(rodadaId)
            .then((dados) => ativo && setRodada(dados))
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [rodadaId]);

    // depois de um envio, leva o foco (e a rolagem) até o resumo
    useEffect(() => {
        resumoRef.current?.focus();
    }, [resumo]);

    if (erro) {
        return (
            <Card>
                <StatusMessage role="alert" className="error">{erro}</StatusMessage>
            </Card>
        );
    }

    if (!rodada) {
        return (
            <Card>
                <StatusMessage role="status">Carregando rodada...</StatusMessage>
            </Card>
        );
    }

    const acertouNesteEnvio = (exercicioId) => resultados[exercicioId]?.correta === true;

    const responder = (exercicioId, resposta) => {
        setRespostas((atuais) => ({ ...atuais, [exercicioId]: resposta }));
        // mudou a resposta de uma questão que errou: o feedback antigo sai e ela volta a ser enviável
        setResultados((atuais) => {
            if (!atuais[exercicioId]) return atuais;
            const semEsta = { ...atuais };
            delete semEsta[exercicioId];
            return semEsta;
        });
    };

    // T2: envia só o que foi respondido e ainda não foi corrigido neste painel
    const pendentes = rodada.exercicios.filter(
        (exercicio) => estaRespondida(exercicio, respostas[exercicio.id]) && !resultados[exercicio.id]
    );

    const enviar = async () => {
        setEnviando(true);
        setErroEnvio(null);
        try {
            const correcao = await trilhaApi.enviarRespostas(
                rodada.id,
                pendentes.map((exercicio) => ({ exercicioId: exercicio.id, resposta: respostas[exercicio.id] }))
            );
            setResultados((atuais) => {
                const novos = { ...atuais };
                correcao.resultados.forEach((resultado) => {
                    novos[resultado.exercicioId] = resultado;
                });
                return novos;
            });
            setResumo(correcao.resumo);
            aoEnviar();
        } catch (e) {
            setErroEnvio(e.message); // as respostas continuam na tela para tentar de novo
        } finally {
            setEnviando(false);
        }
    };

    const irPara = (novaEtapa) => {
        setEtapa(novaEtapa);
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    const proximaRodada = resumo?.proximaRodadaLiberadaId
        ? localizarRodada(resumo.proximaRodadaLiberadaId)
        : null;

    return (
        <Card>
            <CardHeader>
                <div>
                    <span>{etapa === "teoria" ? "Teoria + exemplos" : "Desafios práticos"}</span>
                    <h2>{rodada.titulo}</h2>
                </div>

                <div>
                    <OutlineButton
                        type="button"
                        className={etapa === "teoria" ? "active" : ""}
                        onClick={() => irPara("teoria")}
                    >
                        Ver teoria
                    </OutlineButton>
                    <OutlineButton
                        type="button"
                        className={etapa === "exercicios" ? "active" : ""}
                        onClick={() => irPara("exercicios")}
                    >
                        Ir para exercícios
                    </OutlineButton>
                </div>
            </CardHeader>

            {etapa === "teoria" && (
                <>
                    <TeoriaRodada rodada={rodada} />
                    <BottomActions>
                        <PrimaryButton type="button" onClick={() => irPara("exercicios")}>
                            Avançar para desafios
                        </PrimaryButton>
                    </BottomActions>
                </>
            )}

            {etapa === "exercicios" && (
                <>
                    {somenteLeitura && (
                        <StatusMessage role="note">
                            Modo visualização: professores e administradores veem os exercícios, mas não respondem.
                        </StatusMessage>
                    )}

                    <ExerciseList>
                        {rodada.exercicios.map((exercicio, index) => (
                            <ExercicioCard
                                key={exercicio.id}
                                exercicio={exercicio}
                                numero={index + 1}
                                concluido={exercicio.concluido || acertouNesteEnvio(exercicio.id)}
                                resultado={resultados[exercicio.id]}
                            >
                                <CampoResposta
                                    exercicio={exercicio}
                                    valor={respostas[exercicio.id]}
                                    aoMudar={(resposta) => responder(exercicio.id, resposta)}
                                    // acertou agora: trava (reenviar não daria XP e contaria tentativa)
                                    desabilitado={somenteLeitura || enviando || acertouNesteEnvio(exercicio.id)}
                                />
                            </ExercicioCard>
                        ))}
                    </ExerciseList>

                    {resumo && (
                        <div ref={resumoRef} tabIndex={-1}>
                            <ResultadoRodada
                                resumo={resumo}
                                proximaRodada={proximaRodada}
                                aoIrParaProxima={() => aoIrParaRodada(proximaRodada)}
                            />
                        </div>
                    )}

                    {erroEnvio && (
                        <StatusMessage role="alert" className="error">{erroEnvio}</StatusMessage>
                    )}

                    {!somenteLeitura && (
                        <SubmitInfo>
                            {pendentes.length === 0
                                ? resumo
                                    ? "Ajuste as questões que errou ou responda outras para verificar de novo."
                                    : "Responda pelo menos uma questão para verificar."
                                : `${pendentes.length} ${pendentes.length === 1 ? "resposta pronta" : "respostas prontas"} para verificar. Questões em branco não são enviadas.`}
                        </SubmitInfo>
                    )}

                    <BottomActions>
                        <OutlineButton type="button" onClick={() => irPara("teoria")}>
                            Voltar para teoria
                        </OutlineButton>

                        {!somenteLeitura && (
                            <PrimaryButton
                                type="button"
                                disabled={pendentes.length === 0 || enviando}
                                onClick={enviar}
                            >
                                {enviando ? "Verificando..." : "Verificar respostas"}
                            </PrimaryButton>
                        )}
                    </BottomActions>
                </>
            )}
        </Card>
    );
};

export default PainelRodada;
