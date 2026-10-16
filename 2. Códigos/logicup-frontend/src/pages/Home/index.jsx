import { useNavigate } from "react-router-dom";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import HeroHome from "../../components/home/HeroHome";
import { useAuth } from "../../context/useAuth";
import { catalogoFases } from "../../data/catalogoFases";
import { iconeFase } from "../../assets/fases";
import {
    Section,
    SectionTitle,
    Passos,
    Passo,
    FaixaFases,
    FaseMini,
    BlogSection,
    BlogGrid,
    BlogCard,
    BlogTag,
    Chamada,
} from "./styles";

/* "Como funciona": o ciclo de aprendizagem da plataforma, com os ícones do Davi */
const PASSOS = [
    { icone: "puzzle", cor: "yellow", titulo: "Estude a teoria", texto: "Cada rodada começa com uma explicação curta, objetivos e exemplos de código." },
    { icone: "truefalse", cor: "yellow", titulo: "Responda desafios", texto: "Uma questão por vez, com feedback na hora e a explicação de cada resposta." },
    { icone: "order", cor: "green", titulo: "Ganhe XP e avance", texto: "Acertou? Ganha XP. Aprove as rodadas para desbloquear a próxima fase da trilha." },
    { icone: "boss", cor: "orange", titulo: "Vença o Boss", texto: "Encare os desafios finais de cada fase e suba no ranking global." },
];

/* "Gotas de saber": textos curtos para começar (sem botões: ainda não há artigos completos) */
const GOTAS = [
    {
        tag: "Lógica",
        titulo: "O que é lógica de programação?",
        texto: "É a organização de passos para resolver um problema. Antes de escrever código, é importante entender a ordem das ações, as decisões envolvidas e o resultado esperado.",
    },
    {
        tag: "Java",
        titulo: "Por que começar com Java?",
        texto: "Java é uma linguagem muito usada no mercado e ajuda a entender conceitos como variáveis, métodos, classes e objetos de forma estruturada.",
    },
    {
        tag: "Algoritmos",
        titulo: "Algoritmo não é só código",
        texto: "Um algoritmo pode ser uma receita, uma sequência de tarefas ou um fluxograma. O código é apenas uma das formas de representar essa solução.",
    },
];

const Home = () => {
    const navigate = useNavigate();
    const { usuario } = useAuth();
    const irParaJornada = () => navigate(usuario ? "/trilha" : "/cadastro");

    return (
        <>
            <Header />
            <HeroHome />

            <Section>
                <SectionTitle>
                    <span>Como funciona?</span>
                    <h2>Estude Java como se estivesse em um jogo</h2>
                    <p>Quatro passos que se repetem em cada fase da trilha.</p>
                </SectionTitle>

                <Passos>
                    {PASSOS.map((passo, indice) => (
                        <Passo key={passo.titulo}>
                            <span className="numero">{indice + 1}</span>
                            <img src={iconeFase(passo.icone, passo.cor)} alt="" />
                            <h3>{passo.titulo}</h3>
                            <p>{passo.texto}</p>
                        </Passo>
                    ))}
                </Passos>
            </Section>

            <Section className="trilha">
                <SectionTitle className="light">
                    <span>A trilha</span>
                    <h2>10 fases, da lógica à orientação a objetos</h2>
                    <p>Cada fase libera a seguinte. No fim, o Boss final espera por você.</p>
                </SectionTitle>

                <FaixaFases aria-label="As 10 fases da trilha">
                    {catalogoFases.map((fase) => (
                        <FaseMini key={fase.ordem} className={fase.boss ? "boss" : ""}>
                            <img src={iconeFase(fase.icone, fase.boss ? "orange" : "yellow")} alt="" />
                            <span>Fase {fase.ordem}</span>
                            <strong>{fase.titulo}</strong>
                        </FaseMini>
                    ))}
                </FaixaFases>
            </Section>

            <BlogSection>
                <SectionTitle className="light">
                    <span>Gotas de saber</span>
                    <h2>Conteúdos rápidos para começar na programação</h2>
                    <p>Mini textos para revisar conceitos importantes antes de entrar na trilha.</p>
                </SectionTitle>

                <BlogGrid>
                    {GOTAS.map((gota) => (
                        <BlogCard key={gota.titulo}>
                            <BlogTag>{gota.tag}</BlogTag>
                            <h3>{gota.titulo}</h3>
                            <p>{gota.texto}</p>
                        </BlogCard>
                    ))}
                </BlogGrid>
            </BlogSection>

            <Chamada>
                <h2>{usuario ? "Sua próxima missão está esperando." : "Pronto para a primeira missão?"}</h2>
                <button type="button" onClick={irParaJornada}>
                    {usuario ? "Continuar trilha →" : "Criar minha conta"}
                </button>
            </Chamada>

            <Footer />
        </>
    );
};

export default Home;
