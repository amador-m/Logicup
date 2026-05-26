import Header from "../../components/layout/Header";
import Banner from "../../components/layout/Banner";
import Footer from "../../components/layout/Footer";
import {
    Section,
    SectionTitle,
    Cards,
    Card,
    PathSection,
    MissionNode,
    Line,
} from "./styles";

const Home = () => {
    return (
        <>
            <Header />
            <Banner />

            <Section>
                <SectionTitle>
                    <span>Como funciona?</span>
                    <h2>Estude Java como se estivesse em um jogo</h2>
                    <p>
                        Complete missões, ganhe pontos, desbloqueie recompensas e acompanhe
                        sua evolução na trilha de aprendizado
                    </p>
                </SectionTitle>

                <Cards>
                    <Card>
                        <h3>Missões</h3>
                        <p>Conteúdos teóricos e exercícios práticos organizados por níveis</p>
                    </Card>

                    <Card>
                        <h3>Ranking</h3>
                        <p>Veja sua colocação e acompanhe o desempenho da sua turma</p>
                    </Card>

                    <Card>
                        <h3>Recompensas</h3>
                        <p>Desbloqueie medalhas, conquistas e itens conforme avança</p>
                    </Card>
                </Cards>
            </Section>

            <PathSection>
                <h2>Prévia da trilha/mapa de fases</h2>

                <MissionNode>1</MissionNode>
                <Line />
                <MissionNode className="orange">2</MissionNode>
                <Line />
                <MissionNode>3</MissionNode>
                <Line />
                <MissionNode className="locked">4</MissionNode>
            </PathSection>

            <Footer />
        </>
    );
};

export default Home;