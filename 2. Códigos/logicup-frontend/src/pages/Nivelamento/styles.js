import styled from "styled-components";

/* Sprint 11: a página do nivelamento reaproveita o fundo e os cartões da Missão (mesma linguagem visual) */

export const Topo = styled.div`
  max-width: 820px;
  margin: 0 auto 22px;
  color: var(--cream);

  span {
    color: var(--yellow);
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: 1px;
    font-size: 14px;
  }

  h1 {
    font-size: 40px;
    margin: 8px 0 6px;
    line-height: 1.1;
  }

  p {
    color: rgba(241, 240, 226, 0.85);
    line-height: 1.6;
  }

  @media (max-width: 600px) {
    h1 {
      font-size: 30px;
    }
  }
`;

export const Cartao = styled.article`
  max-width: 820px;
  margin: 0 auto;
  background: var(--cream);
  color: var(--dark);
  border: 4px solid var(--yellow);
  border-radius: 30px;
  padding: 30px;
  box-shadow: 0 18px 45px rgba(4, 4, 35, 0.28);

  h2 {
    font-size: 22px;
    line-height: 1.4;
    margin-bottom: 14px;
  }

  @media (max-width: 600px) {
    padding: 22px 18px;
    border-radius: 24px;
  }
`;

export const Progresso = styled.div`
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
  font-weight: 800;
  color: var(--brown-dark);
  font-size: 14px;

  .barra {
    flex: 1;
    height: 10px;
    border-radius: 999px;
    background: rgba(118, 73, 53, 0.18);
    overflow: hidden;
  }

  .barra span {
    display: block;
    height: 100%;
    background: var(--green);
    transition: width 0.3s ease;
  }

  @media (prefers-reduced-motion: reduce) {
    .barra span {
      transition: none;
    }
  }
`;

export const Acoes = styled.div`
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 24px;
  flex-wrap: wrap;
`;

export const Mascote = styled.div`
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 18px;

  img {
    width: 88px;
    flex-shrink: 0;
  }

  p {
    line-height: 1.6;
  }
`;

export const Placar = styled.p`
  font-size: 20px;
  font-weight: 900;
  color: var(--green);
  margin: 6px 0 4px;
`;

export const ListaCorrecoes = styled.ol`
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  list-style: none;
  padding: 0;

  li {
    border-radius: 16px;
    padding: 14px 16px;
    border-left: 6px solid var(--green);
    background: rgba(40, 128, 120, 0.1);
    line-height: 1.5;
  }

  li.errou {
    border-left-color: var(--orange);
    background: rgba(227, 92, 51, 0.1);
  }

  li strong {
    display: block;
    margin-bottom: 4px;
  }
`;

/* botão discreto em fundo creme (o SecondaryButton da Missão é para fundo escuro) */
export const BotaoTexto = styled.button`
  background: transparent;
  border: 2px solid var(--brown-dark);
  color: var(--brown-dark);
  padding: 13px 22px;
  border-radius: 18px;
  font-weight: 800;
  cursor: pointer;

  &:hover:not(:disabled) {
    background: rgba(118, 73, 53, 0.1);
  }

  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;
