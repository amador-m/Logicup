import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer/index.jsx";
import { useAuth } from "../../context/useAuth";
import { trilhaApi } from "../../services/api";
import { STATUS_FASE, iniciais, montarMapa } from "../../util/trilha";
import {
    Page,
    Hero,
    UserInfo,
    Avatar,
    Stats,
    StatCard,
    ViewModeInfo,
    MapWrapper,
    Path,
    PhaseCard,
    MissionButton,
    Connector,
    Content,
    Badge,
    Topics,
    PhaseProgress,
    ActionButton,
    StatusMessage,
} from "./styles";

const TEXTO_BADGE = {
    [STATUS_FASE.CONCLUIDA]: "Concluída",
    [STATUS_FASE.DISPONIVEL]: "Disponível",
    [STATUS_FASE.BLOQUEADA]: "Bloqueada",
    [STATUS_FASE.EM_BREVE]: "Em breve",
};

/** Só fases que existem e não estão bloqueadas podem ser abertas. */
const podeAbrir = (fase) =>
    fase.status === STATUS_FASE.DISPONIVEL || fase.status === STATUS_FASE.CONCLUIDA;

function iconeDoBotao(fase) {
    if (fase.status === STATUS_FASE.BLOQUEADA) return "🔒";
    if (fase.status === STATUS_FASE.EM_BREVE) return "⏳";
    if (fase.status === STATUS_FASE.CONCLUIDA) return "✓";
    return fase.boss ? "👾" : fase.ordem;
}

function textoDaAcao(fase, somenteLeitura) {
    if (fase.status === STATUS_FASE.EM_BREVE) return "Em breve";
    if (fase.status === STATUS_FASE.BLOQUEADA) return "Bloqueada";
    if (somenteLeitura) return "Ver conteúdo";
    if (fase.status === STATUS_FASE.CONCLUIDA) return "Revisar missão";
    return fase.exerciciosConcluidos > 0 ? "Continuar missão" : "Iniciar missão";
}

const LearningPath = () => {
    const navigate = useNavigate();
    const { usuario } = useAuth();

    const [trilha, setTrilha] = useState(null);   // resposta de GET /api/trilha
    const [erro, setErro] = useState(null);        // mensagem de erro para a tela
    const [carregando, setCarregando] = useState(true);
    const [tentativa, setTentativa] = useState(0); // muda -> o efeito busca de novo

    useEffect(() => {
        let ativo = true; // se o usuário sair da tela antes da resposta, não atualiza nada

        trilhaApi.trilha()
            .then((dados) => ativo && setTrilha(dados))
            .catch((e) => ativo && setErro(e.message))
            .finally(() => ativo && setCarregando(false));

        return () => {
            ativo = false;
        };
    }, [tentativa]);

    const tentarDeNovo = () => {
        setErro(null);
        setCarregando(true);
        setTentativa((valor) => valor + 1);
    };

    const aluno = trilha?.aluno;          // null para professor/admin (T4)
    const somenteLeitura = trilha && !aluno; // T3: professor/admin só visualizam
    const fases = trilha ? montarMapa(trilha.fases) : [];

    const abrirFase = (fase) => {
        if (podeAbrir(fase)) {
            navigate(`/missao/${fase.ordem}`);
        }
    };

    return (
        <>
            <Header />

            <Page>
                <Hero>
                    <div>
                        <span>Trilha de aprendizado</span>
                        <h1>Mapa de missões Java</h1>
                        <p>
                            Avance pelas fases, conclua desafios e desbloqueie novos conteúdos
                            na sua jornada pelo universo da programação
                        </p>
                    </div>

                    <UserInfo>
                        <Avatar aria-hidden="true">{iniciais(usuario?.nome)}</Avatar>

                        {aluno && (
                            <Stats>
                                <StatCard>
                                    <strong>{aluno.xp}</strong>
                                    <span>XP</span>
                                </StatCard>

                                <StatCard>
                                    <strong>{aluno.nivel}</strong>
                                    <span>Fase atual</span>
                                </StatCard>

                                <StatCard>
                                    <strong>{aluno.streak}</strong>
                                    <span>Ofensiva</span>
                                </StatCard>
                            </Stats>
                        )}

                        {somenteLeitura && (
                            <ViewModeInfo>
                                <strong>Modo visualização</strong>
                                <span>Você vê todo o conteúdo, mas não responde exercícios.</span>
                            </ViewModeInfo>
                        )}
                    </UserInfo>
                </Hero>

                <MapWrapper>
                    {carregando && <StatusMessage role="status">Carregando sua trilha...</StatusMessage>}

                    {!carregando && erro && (
                        <StatusMessage role="alert" className="error">
                            <p>{erro}</p>
                            <ActionButton type="button" onClick={tentarDeNovo}>
                                Tentar novamente
                            </ActionButton>
                        </StatusMessage>
                    )}

                    {!carregando && !erro && (
                        <Path>
                            {fases.map((fase, index) => (
                                <div key={fase.ordem}>
                                    <PhaseCard className={`${index % 2 === 0 ? "left" : "right"} ${fase.status}`}>
                                        <MissionButton
                                            type="button"
                                            className={`${fase.status} ${fase.boss && podeAbrir(fase) ? "boss" : ""}`}
                                            disabled={!podeAbrir(fase)}
                                            onClick={() => abrirFase(fase)}
                                            aria-label={`Fase ${fase.ordem}: ${fase.titulo} (${TEXTO_BADGE[fase.status]})`}
                                        >
                                            {iconeDoBotao(fase)}
                                        </MissionButton>

                                        <Content className={fase.status}>
                                            <Badge className={fase.status}>{TEXTO_BADGE[fase.status]}</Badge>

                                            <h2>Fase {fase.ordem}</h2>
                                            <h3>{fase.titulo}</h3>

                                            <Topics>
                                                {fase.topicos.map((topico) => (
                                                    <li key={topico}>{topico}</li>
                                                ))}
                                            </Topics>

                                            {fase.existe && aluno && (
                                                <PhaseProgress>
                                                    {fase.exerciciosConcluidos} de {fase.totalExercicios} exercícios
                                                    {" • "}
                                                    {fase.xpGanho}/{fase.xpTotal} XP
                                                </PhaseProgress>
                                            )}

                                            <ActionButton
                                                type="button"
                                                disabled={!podeAbrir(fase)}
                                                onClick={() => abrirFase(fase)}
                                            >
                                                {textoDaAcao(fase, somenteLeitura)}
                                            </ActionButton>
                                        </Content>
                                    </PhaseCard>

                                    {index < fases.length - 1 && <Connector />}
                                </div>
                            ))}
                        </Path>
                    )}
                </MapWrapper>
            </Page>
            <Footer />
        </>
    );
};

export default LearningPath;
