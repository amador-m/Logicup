import Header from "../../components/layout/Header";
import Banner from "../../components/layout/Banner";
import Footer from "../../components/layout/Footer";
import {
    Section,
    SectionTitle,
    Cards,
    Card,
    BlogSection,
    BlogGrid,
    BlogCard,
    BlogTag,
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

            <BlogSection>
                <SectionTitle className="light">
                    <span>Gotas de saber</span>
                    <h2>Conteúdos rápidos para começar na programação</h2>
                    <p>
                        Mini artigos para revisar conceitos importantes antes de entrar na trilha.
                    </p>
                </SectionTitle>

                <BlogGrid>
                    <BlogCard>
                        <BlogTag>Lógica</BlogTag>
                        <h3>O que é lógica de programação?</h3>
                        <p>
                            Lógica de programação é a organização de passos para resolver um problema.
                            Antes de escrever código, é importante entender a ordem das ações, as
                            decisões envolvidas e o resultado esperado.
                        </p>
                        <button>Ler mais</button>
                    </BlogCard>

                    <BlogCard>
                        <BlogTag>Java</BlogTag>
                        <h3>Por que começar com Java?</h3>
                        <p>
                            Java é uma linguagem muito usada no mercado e ajuda o aluno a entender
                            conceitos como variáveis, métodos, classes e objetos de forma estruturada.
                        </p>
                        <button>Ler mais</button>
                    </BlogCard>

                    <BlogCard>
                        <BlogTag>Algoritmos</BlogTag>
                        <h3>Algoritmo não é só código</h3>
                        <p>
                            Um algoritmo pode ser uma receita, uma sequência de tarefas ou um
                            fluxograma. O código é apenas uma das formas de representar essa solução.
                        </p>
                        <button>Ler mais</button>
                    </BlogCard>
                </BlogGrid>
            </BlogSection>

            <Footer />
        </>
    );
};

export default Home;