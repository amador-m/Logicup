import { useEffect, useRef, useState } from "react";
import { trilhaApi } from "../../../services/api";
import TeoriaRodada from "./TeoriaRodada";
import FluxoQuestoes from "./FluxoQuestoes";
import ConclusaoRodada from "./ConclusaoRodada";
import {
    Card,
    CardHeader,
    BottomActions,
    PrimaryButton,
    LinkButton,
    StatusMessage,
} from "../styles";

/*
 * Situação de cada questão PARA O ALUNO, calculada com o que a API já devolve (sem mudar o backend):
 *   concluido = true                  -> "correta"  (PR1: acertou alguma vez; nunca volta atrás)
 *   tentativas > 0 e não concluído    -> "errada"   (já respondeu e ainda não acertou)
 *   tentativas = 0                    -> "pendente" (nunca respondeu)
 * É isso que permite "Refazer as que errei" sem confundir errada com não respondida,
 * inclusive quando o aluno sai e volta outro dia.
 */
function estadoInicial(exercicio) {
    if (exercicio.concluido) return "correta";
    return exercicio.tentativas > 0 ? "errada" : "pendente";
}

const idsCom = (exercicios, estados, estado) =>
    exercicios.filter((exercicio) => estados[exercicio.id] === estado).map((exercicio) => exercicio.id);

/*
 * Conteúdo de UMA rodada (D-S7-1: uma questão por vez, feedback imediato).
 * Etapas:  "teoria" -> "questoes" (FluxoQuestoes) -> "conclusao" (ConclusaoRodada)
 *
 * Todo o estado da rodada fica AQUI (e não nos filhos) para o aluno poder ir em "Rever teoria"
 * no meio das questões e voltar exatamente onde estava, com a resposta que já tinha marcado.
 * A página cria este componente com key={rodadaId}: ao trocar de rodada, tudo recomeça do zero.
 *
 * Quem decide acerto, XP e aprovação é SEMPRE o backend; aqui só exibimos o que ele responde.
 */
const PainelRodada = ({
    rodadaId,
    somenteLeitura,   // T3: professor/admin
    rodadaResumo,     // resumo desta rodada na trilha: status, xpGanho, xpTotal (reais)
    fase,             // { ordem, titulo, xpGanho, xpTotal, totalRodadas }
    ehUltimaDaFase,
    proximaRodada,    // rodada seguinte na trilha (pode ser de outra fase) ou null
    aoEnviar,         // avisa a página para atualizar a trilha (abas, cadeados, XP)
    aoIrParaRodada,
    aoVoltarTrilha,
}) => {
    const [rodada, setRodada] = useState(null);
    const [erro, setErro] = useState(null);
    const [etapa, setEtapa] = useState("teoria");
    const [estados, setEstados] = useState({});
    const [passagem, setPassagem] = useState(null);     // { tipo, fila, indice } ou null
    const [respostas, setRespostas] = useState({});
    const [resultados, setResultados] = useState({});
    const [aprovada, setAprovada] = useState(false);
    const [aprovadaAgora, setAprovadaAgora] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [erroEnvio, setErroEnvio] = useState(null);
    const cardRef = useRef(null);

    useEffect(() => {
        let ativo = true;
        trilhaApi.rodada(rodadaId)
            .then((dados) => {
                if (!ativo) return;
                const iniciais = Object.fromEntries(dados.exercicios.map((e) => [e.id, estadoInicial(e)]));
                setRodada(dados);
                setEstados(iniciais);
                setAprovada(dados.status === "APROVADA");

                // Onde abrir: aluno que já começou a rodada volta direto para as questões (ou o resultado)
                const pendentes = idsCom(dados.exercicios, iniciais, "pendente");
                const jaComecou = pendentes.length < dados.exercicios.length;
                if (!somenteLeitura && jaComecou) {
                    if (pendentes.length > 0) {
                        setPassagem({ tipo: "normal", fila: pendentes, indice: 0 });
                        setEtapa("questoes");
                    } else {
                        setEtapa("conclusao");
                    }
                }
            })
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [rodadaId, somenteLeitura]);

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

    const exercicios = rodada.exercicios;
    const emAndamento = passagem !== null && etapa !== "conclusao";

    // troca de etapa: rola até o INÍCIO DO CARD (e não o topo da página), para o resultado ficar à vista
    const irPara = (novaEtapa) => {
        setEtapa(novaEtapa);
        cardRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
    };

    /** Começa uma passagem pelas questões da fila (limpando respostas e feedback antigos delas). */
    const iniciarPassagem = (tipo, fila) => {
        if (fila.length === 0) {
            irPara("conclusao");
            return;
        }
        if (tipo !== "revisar") {
            setRespostas((atuais) => semAsChaves(atuais, fila));
            setResultados((atuais) => semAsChaves(atuais, fila));
        }
        setErroEnvio(null);
        setPassagem({ tipo, fila, indice: 0 });
        irPara("questoes");
    };

    const comecarDesafios = () => {
        if (somenteLeitura) {
            iniciarPassagem("revisar", exercicios.map((e) => e.id));
        } else {
            iniciarPassagem("normal", idsCom(exercicios, estados, "pendente"));
        }
    };

    const responder = (exercicioId, resposta) => {
        setRespostas((atuais) => ({ ...atuais, [exercicioId]: resposta }));
    };

    /** POST com UMA resposta (T2 já permite). O backend corrige, grava progresso/XP e diz se aprovou. */
    const verificar = async (exercicio) => {
        setEnviando(true);
        setErroEnvio(null);
        try {
            const correcao = await trilhaApi.enviarRespostas(rodada.id, [
                { exercicioId: exercicio.id, resposta: respostas[exercicio.id] },
            ]);
            const resultado = correcao.resultados[0];
            setResultados((atuais) => ({ ...atuais, [exercicio.id]: resultado }));
            setEstados((atuais) => ({
                ...atuais,
                // PR1: uma questão concluída nunca volta a ser "errada"
                [exercicio.id]: resultado.correta || atuais[exercicio.id] === "correta" ? "correta" : "errada",
            }));
            if (correcao.resumo.rodadaAprovada && !aprovada) {
                setAprovada(true);
                setAprovadaAgora(true); // comemora só na transição
            }
            aoEnviar();
        } catch (e) {
            setErroEnvio(e.message); // a resposta continua marcada para tentar de novo
        } finally {
            setEnviando(false);
        }
    };

    const avancar = () => {
        if (passagem.indice < passagem.fila.length - 1) {
            setPassagem({ ...passagem, indice: passagem.indice + 1 });
        } else if (passagem.tipo === "revisar" && somenteLeitura) {
            setPassagem(null);
            irPara("teoria");
        } else {
            irPara("conclusao");
        }
    };

    const voltar = () => setPassagem({ ...passagem, indice: Math.max(0, passagem.indice - 1) });

    let tituloEtapa = "Teoria + exemplos";
    if (etapa === "questoes") tituloEtapa = somenteLeitura ? "Questões (modo visualização)" : "Desafios práticos";
    if (etapa === "conclusao") tituloEtapa = "Resultado da rodada";

    return (
        <Card ref={cardRef}>
            <CardHeader>
                <div>
                    <span>{tituloEtapa}</span>
                    <h2>{rodada.titulo}</h2>
                </div>

                {/* a 2ª div existe sempre: o CardHeader alinha título à esquerda e ações à direita */}
                <div>
                    {etapa === "questoes" && (
                        <LinkButton type="button" onClick={() => irPara("teoria")}>
                            Rever teoria
                        </LinkButton>
                    )}
                </div>
            </CardHeader>

            {etapa === "teoria" && (
                <>
                    <TeoriaRodada rodada={rodada} />
                    <BottomActions>
                        {emAndamento ? (
                            <PrimaryButton type="button" onClick={() => irPara("questoes")}>
                                Voltar para a questão {passagem.indice + 1}
                            </PrimaryButton>
                        ) : (
                            <PrimaryButton type="button" onClick={comecarDesafios}>
                                {somenteLeitura ? "Ver questões" : "Começar desafios"}
                            </PrimaryButton>
                        )}
                    </BottomActions>
                </>
            )}

            {etapa === "questoes" && passagem && (
                <FluxoQuestoes
                    exercicios={exercicios}
                    passagem={passagem}
                    estados={estados}
                    respostas={respostas}
                    resultados={resultados}
                    rodadaResumo={rodadaResumo}
                    enviando={enviando}
                    erroEnvio={erroEnvio}
                    aoResponder={responder}
                    aoVerificar={verificar}
                    aoAvancar={avancar}
                    aoVoltar={voltar}
                />
            )}

            {etapa === "conclusao" && (
                <ConclusaoRodada
                    exercicios={exercicios}
                    estados={estados}
                    aprovada={aprovada}
                    aprovadaAgora={aprovadaAgora}
                    ehUltimaDaFase={ehUltimaDaFase}
                    fase={fase}
                    rodadaResumo={rodadaResumo}
                    proximaRodada={proximaRodada}
                    aoRefazerErradas={() => iniciarPassagem("refazer", idsCom(exercicios, estados, "errada"))}
                    aoContinuar={() => iniciarPassagem("normal", idsCom(exercicios, estados, "pendente"))}
                    aoVerQuestoes={() => iniciarPassagem("revisar", exercicios.map((e) => e.id))}
                    aoReverTeoria={() => {
                        setPassagem(null);
                        irPara("teoria");
                    }}
                    aoIrParaProxima={() => aoIrParaRodada(proximaRodada)}
                    aoVoltarTrilha={aoVoltarTrilha}
                />
            )}
        </Card>
    );
};

/** Cópia do objeto sem as chaves informadas (usado para "zerar" respostas/feedback ao refazer). */
function semAsChaves(objeto, chaves) {
    const copia = { ...objeto };
    chaves.forEach((chave) => delete copia[chave]);
    return copia;
}

export default PainelRodada;
