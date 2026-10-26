import styled, { keyframes } from "styled-components";

/* mesma faixa de título e fundo da trilha, do ranking e do perfil: tudo faz parte do mesmo "jogo" */
export { Page, Hero } from "../LearningPath/styles";

const flutuar = keyframes`
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-6px); }
`;

export const CartaoGemas = styled.div`
  min-width: 260px;
  padding: 22px 24px;
  border-radius: 24px;
  background: var(--cream);
  color: var(--dark);
  text-align: center;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.3);

  .saldo {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    font-size: 44px;
    font-weight: 900;
    color: var(--green);
  }

  small {
    display: block;
    margin: 2px 0 14px;
    font-weight: 700;
    color: var(--brown-dark);
  }

  @media (max-width: 900px) {
    width: 100%;
  }
`;

export const BotaoLoja = styled.button`
  width: 100%;
  padding: 12px 18px;
  border: none;
  border-radius: 16px;
  background: var(--yellow);
  color: var(--dark);
  font-size: 15px;
  font-weight: 900;
  cursor: pointer;
  box-shadow: 0 4px 0 #b07f05;

  &:hover {
    background: var(--orange);
    color: var(--white);
    box-shadow: 0 4px 0 #a83a1b;
  }

  &:focus-visible {
    outline: 3px solid var(--dark);
    outline-offset: 2px;
  }
`;

export const Resumo = styled.p`
  margin: 0 120px 22px;
  color: var(--cream);
  font-weight: 700;

  strong {
    color: var(--yellow);
  }

  @media (max-width: 900px) {
    margin: 0 20px 18px;
  }
`;

export const Vazio = styled.div`
  margin: 0 120px 22px;

  @media (max-width: 1100px) {
    margin: 0 40px 22px;
  }

  @media (max-width: 640px) {
    margin: 0 20px 18px;
  }
`;

export const Grade = styled.ul`
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 20px;
  padding: 0 120px;
  margin: 0;
  list-style: none;

  @media (max-width: 1100px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    padding: 0 40px;
  }

  @media (max-width: 640px) {
    grid-template-columns: minmax(0, 1fr);
    padding: 0 20px;
  }
`;

export const Card = styled.li`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 22px 20px 20px;
  border-radius: 26px;
  background: var(--cream);
  color: var(--dark);
  text-align: center;
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.22);
  border: 4px solid ${({ $completa }) => ($completa ? "var(--yellow)" : "transparent")};

  .moeda {
    width: 120px;
    height: auto;
    filter: drop-shadow(0 8px 10px rgba(118, 73, 53, 0.3));
  }

  /* moeda conquistada "flutua" de leve; a bloqueada fica parada */
  &.ganha .moeda {
    animation: ${flutuar} 3s ease-in-out infinite;
  }

  &.bloqueada .moeda {
    opacity: 0.75;
  }

  h2 {
    font-size: 20px;
    margin: 6px 0 0;
  }

  @media (prefers-reduced-motion: reduce) {
    &.ganha .moeda {
      animation: none;
    }
  }
`;

export const Selo = styled.span`
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.8px;
  text-transform: uppercase;
  color: var(--white);
  background: ${({ $cor }) => $cor};
`;

/* os 3 níveis em miniatura (bronze, prata, ouro) */
export const Niveis = styled.ol`
  display: flex;
  justify-content: center;
  gap: 14px;
  list-style: none;
  padding: 0;
  margin: 10px 0 4px;

  li {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 2px;
    font-size: 11px;
    font-weight: 800;
    color: var(--brown-dark);
  }

  img {
    width: 42px;
    height: auto;
  }

  li.falta {
    opacity: 0.55;
  }
`;

export const Progresso = styled.div`
  width: 100%;
  margin-top: 8px;

  p {
    font-size: 14px;
    font-weight: 700;
    color: var(--brown-dark);
    margin: 0 0 6px;
  }

  p.detalhe {
    margin: 6px 0 0;
    font-size: 13px;
  }
`;

export const Barra = styled.div`
  height: 12px;
  border-radius: 999px;
  background: rgba(118, 73, 53, 0.18);
  overflow: hidden;

  span {
    display: block;
    height: 100%;
    width: ${({ $percentual }) => $percentual}%;
    border-radius: 999px;
    background: linear-gradient(90deg, var(--orange), var(--yellow));
    transition: width 0.6s ease-out;
  }

  @media (prefers-reduced-motion: reduce) {
    span {
      transition: none;
    }
  }
`;

export const Aviso = styled.p`
  margin: 0 120px;
  padding: 18px 22px;
  border-radius: 18px;
  background: var(--cream);
  color: var(--dark);
  font-weight: 700;

  @media (max-width: 900px) {
    margin: 0 20px;
  }
`;
