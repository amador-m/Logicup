import styled, { keyframes } from "styled-components";

const subir = keyframes`
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: none; }
`;

/* Faixa fixa no rodapé da tela. Não é modal: a pessoa pode usar o site sem fechá-la. */
export const Faixa = styled.div`
  position: fixed;
  left: 16px;
  right: 16px;
  bottom: 16px;
  z-index: 40; /* abaixo do Header (50) e do menu do celular */
  max-width: 880px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  border-radius: 20px;
  background: var(--dark);
  color: var(--cream);
  border: 2px solid var(--yellow);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.35);
  animation: ${subir} 0.25s ease-out;

  p {
    font-size: 14px;
    line-height: 1.6;
  }

  a {
    color: var(--yellow);
    font-weight: 700;
    text-decoration: underline;
    text-underline-offset: 3px;
  }

  a:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
    border-radius: 4px;
  }

  @media (max-width: 600px) {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
    left: 12px;
    right: 12px;
    bottom: 12px;
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

export const BotaoEntendi = styled.button`
  flex-shrink: 0;
  padding: 10px 26px;
  border: none;
  border-radius: 999px;
  background: var(--yellow);
  color: var(--dark);
  font-weight: 800;
  font-size: 15px;
  cursor: pointer;

  &:hover {
    background: var(--orange);
    color: var(--cream);
  }

  &:focus-visible {
    outline: 3px solid var(--cream);
    outline-offset: 2px;
  }
`;
