import styled, { keyframes } from "styled-components";

const aparecer = keyframes`
  from { opacity: 0; transform: translateY(24px) scale(0.97); }
  to   { opacity: 1; transform: none; }
`;

export const Dialogo = styled.dialog`
  /* o <dialog> nativo vem com borda e fundo branco: zeramos para o card do formulário aparecer */
  border: none;
  padding: 0;
  background: transparent;
  width: min(1000px, calc(100% - 32px));
  max-height: calc(100dvh - 32px);
  margin: auto;
  /* Sprint 10: o modal inteiro NÃO rola mais (a barra nativa ficava feia e cortava o canto arredondado).
     Quem rola é só a coluna do formulário (RightSide), e o lado colorido fica parado, sempre à vista. */
  overflow: hidden;
  border-radius: 32px;
  animation: ${aparecer} 0.25s ease-out;

  /* fundo escurecido e desfocado: a Home continua visível atrás */
  &::backdrop {
    background: rgba(4, 4, 35, 0.6);
    backdrop-filter: blur(4px);
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

export const BotaoFechar = styled.button`
  position: absolute;
  top: 14px;
  right: 14px;
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: var(--dark);
  color: var(--cream);
  font-size: 18px;
  font-weight: 900;
  cursor: pointer;
  z-index: 5;

  &:hover {
    background: var(--orange);
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;
