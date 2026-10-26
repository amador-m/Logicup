import styled, { keyframes } from "styled-components";

const aparecer = keyframes`
  from { opacity: 0; transform: translateY(24px) scale(0.97); }
  to   { opacity: 1; transform: none; }
`;

/* o Gattuccino "pula" para dentro a cada passo */
const pular = keyframes`
  0%   { opacity: 0; transform: translateY(18px) scale(0.9); }
  60%  { opacity: 1; transform: translateY(-6px) scale(1.03); }
  100% { opacity: 1; transform: none; }
`;

export const Dialogo = styled.dialog`
  width: min(520px, calc(100% - 32px));
  max-height: calc(100vh - 32px);
  margin: auto;
  overflow: auto;
  padding: 26px 32px 24px;
  border: 5px solid var(--yellow);
  border-radius: 32px;
  background: var(--cream);
  color: var(--dark);
  text-align: center;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
  animation: ${aparecer} 0.25s ease-out;

  /*
   * Altura FIXA (pedido da Maria): a janela não muda de tamanho nem "pula" entre os passos.
   * Os valores vêm da medição do passo mais longo de cada perfil, de 320px a 1366px de largura, com folga.
   * Se um texto do passos.js crescer muito, vale medir de novo. Em coluna flexível: o conteúdo fica em cima e os pontos + botões sempre no rodapé.
   * O min() garante que, numa tela baixa, a janela nunca passe da altura visível (aí ela rola por dentro).
   */
  height: min(500px, calc(100vh - 32px)); /* maior passo medido acima de 420px: 480px */

  &[open] {
    display: flex;
    flex-direction: column;
  }

  &::backdrop {
    background: rgba(4, 4, 35, 0.65);
    backdrop-filter: blur(4px);
  }

  h2 {
    font-size: 24px;
    line-height: 1.25;
    margin: 6px 0 10px;
    outline: none; /* recebe o foco por código a cada passo; o destaque visual não ajuda aqui */
  }

  p {
    line-height: 1.65;
  }

  @media (max-width: 600px) {
    padding: 22px 20px 20px;
    border-radius: 26px;

    h2 {
      font-size: 21px;
    }
  }

  /* até 420px os botões empilham e o texto ocupa mais linhas: maior passo medido = 652px (em 320px) */
  @media (max-width: 420px) {
    height: min(680px, calc(100vh - 32px));
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

export const Contador = styled.span`
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--brown-dark);
`;

export const Figuras = styled.div`
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 18px;
  height: 140px;
  margin-top: 10px;

  img {
    max-height: 100%;
    max-width: 150px;
    object-fit: contain;
    animation: ${pular} 0.45s ease-out both;
  }

  img + img {
    animation-delay: 0.12s;
  }

  @media (max-width: 600px) {
    height: 110px;

    img {
      max-width: 120px;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    img {
      animation: none;
    }
  }
`;

export const Pontos = styled.div`
  display: flex;
  justify-content: center;
  gap: 8px;
  margin: 16px 0 18px;

  span {
    width: 10px;
    height: 10px;
    border-radius: 999px;
    background: rgba(118, 73, 53, 0.25);
    transition: width 0.2s;
  }

  span.atual {
    width: 26px;
    background: var(--orange);
  }
`;

/* empurra os pontos e os botões para o rodapé da janela de altura fixa */
export const Espaco = styled.div`
  flex: 1;
`;

export const Botoes = styled.div`
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;

  .navegar {
    display: flex;
    gap: 8px;
    margin-left: auto;
  }

  @media (max-width: 420px) {
    flex-direction: column-reverse;
    align-items: stretch;

    .navegar {
      margin-left: 0;
      flex-direction: column-reverse;
    }
  }
`;

export const BotaoPrincipal = styled.button`
  padding: 13px 24px;
  border: none;
  border-radius: 16px;
  background: var(--green);
  color: var(--white);
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;

  &:hover {
    background: var(--dark);
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;

export const BotaoTexto = styled.button`
  &.oculto {
    visibility: hidden; /* ocupa o espaço, mas não aparece nem recebe clique */
  }

  padding: 13px 14px;
  border: none;
  border-radius: 14px;
  background: transparent;
  color: var(--brown-dark);
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;

  &:hover {
    background: rgba(118, 73, 53, 0.1);
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;
