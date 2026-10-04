import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import { trilhaApi } from "../../services/api";
import PainelRodada from "./components/PainelRodada";
import {
    Page,
    HeaderSection,
    BackButton,
    PhaseTag,
    ProgressArea,
    ProgressBar,
    RoundTabs,
    RoundTab,
    Card,
    StatusMessage,
} from "./styles";

/**
 * Rodada que abre por padrão: a primeira LIBERADA (a que o aluno ainda precisa fazer);
 * se todas estiverem aprovadas, a primeira da fase (revisão).
 */
function rodadaPadrao(rodadas) {
    const liberada = rodadas.find((rodada) => rodada.status === "LIBERADA");
    const acessivel = rodadas.find((rodada) => rodada.status !== "BLOQUEADA");
    return (liberada ?? acessivel)?.id ?? null;
}

/*
 * Página da missão (/missao/:faseId), onde faseId é o NÚMERO da fase (I7).
 * Responsabilidades: descobrir a fase na trilha, desenhar as abas das rodadas
 * e mostrar o painel da rodada escolhida. O conteúdo da rodada é do PainelRodada.
 */
const Mission = () => {
    const navigate = useNavigate();
    const { faseId } = useParams();
    const faseOrdem = Number(faseId);

    const [trilha, setTrilha] = useState(null);
    const [erro, setErro] = useState(null);
    const [rodadaEscolhidaId, setRodadaEscolhidaId] = useState(null); // null = usar a padrão
    const [versaoTrilha, setVersaoTrilha] = useState(0); // muda -> busca a trilha de novo

    useEffect(() => {
        let ativo = true;
        trilhaApi.trilha()
            .then((dados) => ativo && setTrilha(dados))
            .catch((e) => ativo && setErro(e.message));
        return () => {
            ativo = false;
        };
    }, [versaoTrilha]);

    const fase = trilha?.fases.find((item) => item.ordem === faseOrdem) ?? null;
    const somenteLeitura = trilha !== null && trilha.aluno === null; // T3
    const rodadas = fase?.rodadas ?? [];
    // a escolha só vale se a rodada for DESTA fase (ao trocar de fase pela URL, volta ao padrão)
    const escolhaValida = rodadas.some((rodada) => rodada.id === rodadaEscolhidaId);
    const rodadaAtualId = escolhaValida ? rodadaEscolhidaId : rodadaPadrao(rodadas);
    const rodadaAtual = rodadas.find((rodada) => rodada.id === rodadaAtualId) ?? null;

    const concluidos = rodadas.reduce((total, rodada) => total + rodada.exerciciosConcluidos, 0);
    const totalExercicios = rodadas.reduce((total, rodada) => total + rodada.totalExercicios, 0);
    const percentual = totalExercicios > 0 ? Math.round((concluidos / totalExercicios) * 100) : 0;

    const escolherRodada = (rodada) => {
        if (rodada.status !== "BLOQUEADA") {
            setRodadaEscolhidaId(rodada.id);
        }
    };

    /*
     * Depois de um envio: fixa a rodada atual (senão, ao aprovar, a "padrão" passaria a ser a próxima
     * e o painel com o resultado seria trocado) e atualiza a trilha (abas, cadeados, progresso).
     */
    const aposEnvio = () => {
        setRodadaEscolhidaId(rodadaAtualId);
        setVersaoTrilha((versao) => versao + 1);
    };

    /** Onde está uma rodada na trilha: {id, ordem, faseOrdem, mesmaFase}, ou null. */
    const localizarRodada = (rodadaId) => {
        for (const item of trilha?.fases ?? []) {
            const encontrada = item.rodadas.find((rodada) => rodada.id === rodadaId);
            if (encontrada) {
                return { id: rodadaId, ordem: encontrada.ordem, faseOrdem: item.ordem, mesmaFase: item.ordem === faseOrdem };
            }
        }
        return null;
    };

    const irParaRodada = (info) => {
        setRodadaEscolhidaId(info.id);
        if (!info.mesmaFase) {
            navigate(`/missao/${info.faseOrdem}`);
        }
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    // mensagens de estado: carregando, erro, fase inexistente ou bloqueada
    let mensagem = null;
    if (erro) {
        mensagem = <StatusMessage role="alert" className="error">{erro}</StatusMessage>;
    } else if (!trilha) {
        mensagem = <StatusMessage role="status">Carregando missão...</StatusMessage>;
    } else if (!fase) {
        mensagem = <StatusMessage role="alert">Esta fase ainda não está disponível. Ela chega em breve!</StatusMessage>;
    } else if (!rodadaAtual) {
        mensagem = <StatusMessage role="alert">Esta fase ainda está bloqueada. Conclua as fases anteriores para liberá-la.</StatusMessage>;
    }

    return (
        <>
            <Header />

            <Page>
                <HeaderSection>
                    <BackButton type="button" onClick={() => navigate("/trilha")}>
                        ← Voltar para trilha
                    </BackButton>

                    {fase && <PhaseTag>Fase {fase.ordem} • {fase.titulo}</PhaseTag>}

                    <h1>{rodadaAtual?.titulo ?? "Missão"}</h1>

                    <p>
                        Estude a teoria, veja exemplos e resolva os desafios práticos para
                        liberar o próximo round da fase.
                    </p>

                    {rodadaAtual && (
                        <>
                            <ProgressArea>
                                <span>
                                    Round {rodadaAtual.ordem} de {rodadas.length}
                                    {!somenteLeitura && ` • ${concluidos} de ${totalExercicios} exercícios concluídos`}
                                </span>

                                {!somenteLeitura && (
                                    <ProgressBar
                                        role="progressbar"
                                        aria-valuemin={0}
                                        aria-valuemax={100}
                                        aria-valuenow={percentual}
                                        aria-label="Progresso na fase"
                                    >
                                        <div style={{ width: `${percentual}%` }} />
                                    </ProgressBar>
                                )}
                            </ProgressArea>

                            <RoundTabs role="tablist" aria-label="Rounds da fase">
                                {rodadas.map((rodada) => (
                                    <RoundTab
                                        key={rodada.id}
                                        type="button"
                                        role="tab"
                                        aria-selected={rodada.id === rodadaAtualId}
                                        className={rodada.id === rodadaAtualId ? "active" : ""}
                                        disabled={rodada.status === "BLOQUEADA"}
                                        onClick={() => escolherRodada(rodada)}
                                    >
                                        {rodada.status === "BLOQUEADA" && "🔒"}
                                        {rodada.status === "APROVADA" && `✓ Round ${rodada.ordem}`}
                                        {rodada.status === "LIBERADA" && `Round ${rodada.ordem}`}
                                    </RoundTab>
                                ))}
                            </RoundTabs>
                        </>
                    )}
                </HeaderSection>

                {mensagem && <Card>{mensagem}</Card>}

                {!mensagem && (
                    <PainelRodada
                        key={rodadaAtualId}
                        rodadaId={rodadaAtualId}
                        somenteLeitura={somenteLeitura}
                        aoEnviar={aposEnvio}
                        localizarRodada={localizarRodada}
                        aoIrParaRodada={irParaRodada}
                    />
                )}
            </Page>

            <Footer />
        </>
    );
};

export default Mission;
