import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer/index.jsx";
import { useAuth } from "../../context/useAuth";
import { trilhaApi } from "../../services/api";
import { STATUS_FASE, coresDoIcone, iniciais, montarMapa } from "../../util/trilha";
import { iconeFase } from "../../assets/fases";
import MascoteImg from "../../assets/images/MascoteRecortado.png";
import {
    Page,
    Hero,
    UserInfo,
    Avatar,
    Stats,
    StatCard,
    ViewModeInfo,
    Mapa,
    Trilha,
    Caminho,
    Decoracao,
    No,
    BotaoFase,
    Rotulo,
    MascoteAtual,
    ActionButton,
    StatusMessage,
} from "./styles";

/** Só fases que existem e não estão bloqueadas podem ser abertas. */
const podeAbrir = (fase) =>
    fase.status === STATUS_FASE.DISPONIVEL || fase.status === STATUS_FASE.CONCLUIDA;

/* ---------- geometria do mapa ---------- */
const PASSO = 215;       // distância vertical entre duas fases (px)
const TOPO = 10;         // espaço antes da 1ª fase
const CENTRO_ICONE = 58; // altura do centro do ícone dentro do nó (px)
const AMPLITUDE = 26;    // quanto o caminho "balança" para os lados (% da largura)

/** Posição de cada fase: x em % (zigue-zague suave, como uma senoide) e y em px. */
const posicaoDa = (indice) => ({
    x: 50 + AMPLITUDE * Math.sin(indice * 0.95),
    y: TOPO + indice * PASSO,
});

/**
 * Caminho SVG passando pelo centro dos ícones, da fase "de" até a fase "ate".
 * Entre dois pontos usamos uma curva de Bézier (C) com os controles na vertical:
 * isso faz o caminho "serpentear" em vez de ligar os pontos em linha reta.
 */
function desenharCaminho(de, ate) {
    let d = "";
    for (let i = de; i <= ate; i++) {
        const { x, y } = posicaoDa(i);
        const cy = y + CENTRO_ICONE;
        if (i === de) {
            d = `M ${x} ${cy}`;
        } else {
            const anterior = posicaoDa(i - 1);
            const cyAnterior = anterior.y + CENTRO_ICONE;
            d += ` C ${anterior.x} ${cyAnterior + PASSO / 2}, ${x} ${cy - PASSO / 2}, ${x} ${cy}`;
        }
    }
    return d;
}

/*
 * Palavras de Java espalhadas ao fundo do mapa (só decoração).
 * As curtas ficam nas posições em que o nó passa perto do centro (fases 1, 4 e 8), para não encostar nele.
 */
const TRECHOS_JAVA = [
    "{ ; }", "int xp = 0;", "for (int i = 0; ...)", "x++;", "System.out.println();",
    "class Aluno { }", "return true;", "//", "List<Fase> trilha;", "while (aprendendo)",
];

const TEXTO_STATUS = {
    [STATUS_FASE.CONCLUIDA]: "concluída",
    [STATUS_FASE.DISPONIVEL]: "disponível",
    [STATUS_FASE.BLOQUEADA]: "bloqueada",
    [STATUS_FASE.EM_BREVE]: "em breve",
};

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

    /*
     * Fase "atual" = a primeira disponível que ainda não foi concluída (é onde o mascote fica).
     * Se o aluno concluiu tudo o que existe, a atual é a última concluída. Professor/admin não têm fase atual.
     */
    const indiceDisponivel = fases.findIndex((fase) => fase.status === STATUS_FASE.DISPONIVEL);
    const indiceUltimaConcluida = fases.map((fase) => fase.status).lastIndexOf(STATUS_FASE.CONCLUIDA);
    const indiceAtual = somenteLeitura ? -1 : (indiceDisponivel >= 0 ? indiceDisponivel : indiceUltimaConcluida);
    const alturaTrilha = TOPO + fases.length * PASSO;

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

                <Mapa aria-label="Mapa de fases da trilha">
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
                        <Trilha style={{ height: alturaTrilha }}>
                            {/* caminho: base tracejada para a trilha toda + trecho verde até a fase atual */}
                            <Caminho viewBox={`0 0 100 ${alturaTrilha}`} preserveAspectRatio="none" aria-hidden="true">
                                <path className="base" d={desenharCaminho(0, fases.length - 1)} />
                                {indiceAtual > 0 && <path className="percorrido" d={desenharCaminho(0, indiceAtual)} />}
                            </Caminho>

                            {fases.map((fase, indice) => {
                                const { x, y } = posicaoDa(indice);
                                return (
                                    <Decoracao
                                        key={`deco-${fase.ordem}`}
                                        aria-hidden="true"
                                        // sempre do lado OPOSTO ao nó, para não encostar no ícone nem no texto
                                        style={x >= 50 ? { top: y + 40, left: "3%" } : { top: y + 40, right: "3%" }}
                                    >
                                        {TRECHOS_JAVA[indice % TRECHOS_JAVA.length]}
                                    </Decoracao>
                                );
                            })}

                            {fases.map((fase, indice) => {
                                const { x, y } = posicaoDa(indice);
                                const cores = coresDoIcone(fase);
                                const ehAtual = indice === indiceAtual && fase.status === STATUS_FASE.DISPONIVEL;
                                const classes = [fase.status, fase.boss ? "boss" : "", ehAtual ? "atual" : ""].join(" ");

                                return (
                                    <No key={fase.ordem} style={{ left: `${x}%`, top: y }}>
                                        <BotaoFase
                                            type="button"
                                            className={classes}
                                            disabled={!podeAbrir(fase)}
                                            onClick={() => abrirFase(fase)}
                                            aria-label={`Fase ${fase.ordem}: ${fase.titulo} (${TEXTO_STATUS[fase.status]})`}
                                            aria-current={ehAtual ? "step" : undefined}
                                        >
                                            <img className="normal" src={iconeFase(fase.icone, cores.normal)} alt="" />
                                            <img className="press" src={iconeFase(fase.icone, cores.press)} alt="" />
                                        </BotaoFase>

                                        {ehAtual && (
                                            <MascoteAtual className={x > 50 ? "esquerda" : "direita"} aria-hidden="true">
                                                <img src={MascoteImg} alt="" />
                                                <span>{fase.exerciciosConcluidos > 0 ? "Continuar!" : "Começar!"}</span>
                                            </MascoteAtual>
                                        )}

                                        {/* título, progresso e XP sempre visíveis embaixo do nó */}
                                        <Rotulo className={fase.status}>
                                            <span className="numero">Fase {fase.ordem}</span>
                                            <h2>{fase.titulo}</h2>
                                            {fase.existe && aluno && (
                                                <span className="progresso">
                                                    {fase.rodadasAprovadas}/{fase.totalRodadas} rodadas • {fase.xpGanho}/{fase.xpTotal} XP
                                                </span>
                                            )}
                                            {fase.status === STATUS_FASE.BLOQUEADA && <span className="selo">🔒 Bloqueada</span>}
                                            {fase.status === STATUS_FASE.EM_BREVE && <span className="selo">Em breve</span>}
                                        </Rotulo>
                                    </No>
                                );
                            })}
                        </Trilha>
                    )}
                </Mapa>
            </Page>
            <Footer />
        </>
    );
};

export default LearningPath;
