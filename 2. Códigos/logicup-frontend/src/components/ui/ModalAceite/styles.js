import styled, { keyframes } from "styled-components";

const aparecer = keyframes`
  from { opacity: 0; transform: translateY(24px) scale(0.97); }
  to   { opacity: 1; transform: none; }
`;

export const Dialogo = styled.dialog`
  width: min(560px, calc(100% - 32px));
  max-height: calc(100vh - 32px);
  margin: auto;
  overflow: auto;
  padding: 32px 36px;
  border: 5px solid var(--yellow);
  border-radius: 32px;
  background: var(--cream);
  color: var(--dark);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
  animation: ${aparecer} 0.25s ease-out;

  &::backdrop {
    background: rgba(4, 4, 35, 0.7);
    backdrop-filter: blur(4px);
  }

  h2 {
    font-size: 26px;
    margin-bottom: 10px;
    text-align: center;
  }

  p {
    line-height: 1.6;
  }

  .versao {
    margin: 10px 0 20px;
    font-size: 13px;
    font-weight: 600;
    color: var(--brown-dark);
    text-align: center;
  }

  @media (max-width: 600px) {
    padding: 24px 20px;
    border-radius: 24px;

    h2 {
      font-size: 22px;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

/* o mascote "apresenta" o pedido: decorativo para leitores de tela (alt vazio), o texto já diz tudo */
export const Mascote = styled.img`
  display: block;
  width: 96px;
  margin: 0 auto 8px;
`;

export const Mensagem = styled.p`
  margin-top: 12px;
  color: var(--orange);
  font-size: 14px;
  font-weight: 600;
`;

export const Botoes = styled.div`
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 22px;
`;

export const BotaoAceitar = styled.button`
  padding: 15px;
  border: none;
  border-radius: 16px;
  background: var(--green);
  color: var(--white);
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;

  &:hover:not(:disabled) {
    background: var(--dark);
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

export const BotaoSair = styled.button`
  padding: 12px;
  border: 2px solid var(--brown-dark);
  border-radius: 16px;
  background: transparent;
  color: var(--brown-dark);
  font-size: 15px;
  font-weight: 600;
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
