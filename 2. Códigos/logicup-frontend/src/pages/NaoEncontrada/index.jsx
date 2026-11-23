import { Link } from "react-router-dom";
import styled from "styled-components";
import Header from "../../components/layout/Header";
import Footer from "../../components/layout/Footer";
import { Page } from "../LearningPath/styles";
import GattuccinoConfuso from "../../assets/images/onboarding/gattuccino-erro.png";

/* padding (e não margin) no espaço de cima: a margem "vazaria" para fora do Page e mostraria o fundo da página */
const Area = styled.div`
  padding: 60px 16px 0;

  @media (max-width: 600px) {
    padding-top: 32px;
  }
`;

const Cartao = styled.section`
  max-width: 620px;
  margin: 0 auto;
  padding: 36px 40px;
  background: var(--cream);
  border: 5px solid var(--yellow);
  border-radius: 36px;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
  text-align: center;
  color: var(--dark);

  img {
    width: 150px;
    height: auto;
  }

  .codigo {
    display: block;
    margin-top: 8px;
    font-size: 15px;
    font-weight: 800;
    letter-spacing: 1px;
    color: var(--orange);
  }

  h1 {
    font-size: 32px;
    margin: 6px 0 10px;
  }

  p {
    color: var(--brown-dark);
    line-height: 1.6;
    margin-bottom: 22px;
  }

  nav {
    display: flex;
    justify-content: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  nav a {
    padding: 12px 22px;
    border-radius: 16px;
    font-weight: 800;
    text-decoration: none;
    border: 2px solid var(--green);
    color: var(--green);
  }

  nav a.principal {
    background: var(--green);
    color: var(--white);
  }

  nav a:hover {
    background: var(--dark);
    border-color: var(--dark);
    color: var(--white);
  }

  nav a:focus-visible {
    outline: 3px solid var(--orange);
    outline-offset: 2px;
  }

  @media (max-width: 600px) {
    padding: 28px 20px;
    border-radius: 28px;
  }
`;

/*
 * Sprint 11 (revisão da equipe): página para endereço que não existe.
 * Antes, uma URL errada mostrava só o fundo bege, sem nenhuma pista do que tinha acontecido.
 * Fica na rota "*" do AppRoutes: o React Router só usa essa rota quando nenhuma outra combina com o endereço.
 */
const NaoEncontrada = () => (
    <>
        <Header />
        <Page>
            <Area>
                <Cartao aria-labelledby="titulo-404">
                    <img src={GattuccinoConfuso} alt="" />
                    <span className="codigo">ERRO 404</span>
                    <h1 id="titulo-404">Página não encontrada</h1>
                    <p>
                        O Gattuccino procurou em todas as fases e não achou este endereço.
                        Ele pode ter mudado ou estar digitado errado.
                    </p>
                    <nav aria-label="Para onde ir">
                        <Link to="/" className="principal">Voltar ao início</Link>
                        <Link to="/trilha">Ir para a trilha</Link>
                    </nav>
                </Cartao>
            </Area>
        </Page>
        <Footer />
    </>
);

export default NaoEncontrada;
