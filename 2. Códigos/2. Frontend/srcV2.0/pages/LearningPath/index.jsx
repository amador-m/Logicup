import Header from "../../components/layout/Header";
import { useNavigate } from "react-router-dom";
import {
    Page,
    Hero,
    UserInfo,
    Avatar,
    Stats,
    StatCard,
    MapWrapper,
    Path,
    PhaseCard,
    MissionButton,
    Connector,
    Content,
    Badge,
    Topics,
    ActionButton,
} from "./styles";
import Footer from "../../components/layout/Footer/index.jsx";

const phases = [
    {
        id: 1,
        title: "Lógica de programação",
        status: "available", // mostrar se está disponível ou não para o usuário
        topics: ["Algoritmos e fluxogramas", "Sequência lógica", "Pensamento computacional"],
    },
    {
        id: 2,
        title: "Explorando o universo Java",
        status: "locked",
        topics: ["Sintaxe básica", "Estrutura dos códigos", "Entrada e saída de dados"],
    },
    {
        id: 3,
        title: "Variáveis e tipos de dados",
        status: "locked",
        topics: ["Tipos de variáveis", "Operadores matemáticos", "Operadores lógicos"],
    },
    {
        id: 4,
        title: "Estruturas condicionais",
        status: "locked",
        topics: ["if/else", "switch case"],
    },
    {
        id: 5,
        title: "Estruturas de repetição",
        status: "locked",
        topics: ["for", "while", "do while"],
    },
    {
        id: 6,
        title: "Vetores e matrizes",
        status: "locked",
        topics: ["Vetores", "Matrizes", "Percorrer arrays"],
    },
    {
        id: 7,
        title: "Collections",
        status: "locked",
        topics: ["ArrayList", "Vetor x ArrayList", "CRUD em ArrayList"],
    },
    {
        id: 8,
        title: "Métodos",
        status: "locked",
        topics: ["Criação de métodos", "Parâmetros", "Retorno"],
    },
    {
        id: 9,
        title: "Programação Orientada a Objetos",
        status: "locked",
        topics: ["Classes", "Objetos", "Atributos", "Métodos"],
    },
    {
        id: 10,
        title: "Desafio final & Boss Battle",
        status: "boss",
        topics: ["Missões finais", "Desafios cronometrados", "Batalha contra o Chefão"],
    },
];

const LearningPath = () => {
    const navigate = useNavigate();

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
                        <Avatar>LC</Avatar>

                        <Stats>
                            <StatCard>
                                <strong>120</strong>
                                <span>XP</span>
                            </StatCard>

                            <StatCard>
                                <strong>2</strong>
                                <span>Fase atual</span>
                            </StatCard>

                            <StatCard>
                                <strong>4</strong>
                                <span>Ofensiva</span>
                            </StatCard>
                        </Stats>
                    </UserInfo>
                </Hero>

                <MapWrapper>
                    <Path>
                        {phases.map((phase, index) => (
                            <div key={phase.id}>
                                <PhaseCard className={`${index % 2 === 0 ? "left" : "right"} ${phase.status}`}>
                                    <MissionButton className={phase.status}>
                                        {phase.status === "locked" ? "🔒" : phase.status === "boss" ? "👾" : phase.id}
                                    </MissionButton>

                                    <Content>
                                        <Badge>
                                            {phase.status === "locked"
                                                ? "Bloqueada"
                                                : phase.status === "boss"
                                                    ? "Boss Battle"
                                                    : "Disponível"}
                                        </Badge>

                                        <h2>Fase {phase.id}</h2>
                                        <h3>{phase.title}</h3>

                                        <Topics>
                                            {phase.topics.map((topic) => (
                                                <li key={topic}>{topic}</li>
                                            ))}
                                        </Topics>

                                        <ActionButton
                                            disabled={phase.status === "locked"}
                                            onClick={() => navigate(`/missao/${phase.id}`)}
                                        >
                                            {phase.status === "locked" ? "Bloqueada" : "Iniciar missão"}
                                        </ActionButton>
                                    </Content>
                                </PhaseCard>

                                {index < phases.length - 1 && <Connector />}
                            </div>
                        ))}
                    </Path>
                </MapWrapper>
            </Page>
            <Footer />
        </>
    );
};

export default LearningPath;