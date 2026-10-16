import styled, { keyframes } from "styled-components";

const flutuar = keyframes`
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-12px); }
`;

const surgir = keyframes`
  from { opacity: 0; transform: translateY(18px); }
  to   { opacity: 1; transform: none; }
`;

/*
 * Topo da Home. O fundo é feito só com gradientes (nada de imagem externa):
 * carrega na hora e funciona mesmo sem internet na sala da apresentação.
 */
export const Container = styled.section`
  position: relative;
  overflow: hidden;
  min-height: 620px;
  padding: 80px 120px 90px;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  align-items: center;
  gap: 40px;
  background:
    radial-gradient(circle at 12% 20%, rgba(236, 172, 12, 0.28), transparent 28%),
    radial-gradient(circle at 85% 25%, rgba(227, 92, 51, 0.32), transparent 30%),
    radial-gradient(circle at 70% 90%, rgba(40, 128, 120, 0.45), transparent 35%),
    linear-gradient(135deg, var(--dark) 0%, #1a1036 45%, var(--brown-dark) 100%);

  /* "grade" de pontinhos ao fundo, lembrando um editor de código */
  &::before {
    content: "";
    position: absolute;
    inset: 0;
    background-image: radial-gradient(rgba(241, 240, 226, 0.12) 1px, transparent 1px);
    background-size: 26px 26px;
    pointer-events: none;
  }

  @media (max-width: 960px) {
    grid-template-columns: 1fr;
    padding: 56px 24px 64px;
    min-height: auto;
  }
`;

export const Texto = styled.div`
  position: relative;
  z-index: 2;
  color: var(--cream);
  animation: ${surgir} 0.6s ease-out;

  > span {
    display: inline-block;
    background: rgba(236, 172, 12, 0.16);
    border: 1px solid rgba(236, 172, 12, 0.5);
    color: var(--yellow);
    font-weight: 800;
    font-size: 13px;
    text-transform: uppercase;
    letter-spacing: 0.08em;
    padding: 6px 14px;
    border-radius: 999px;
  }

  h1 {
    font-size: 58px;
    line-height: 1.05;
    margin: 20px 0 18px;
  }

  h1 em {
    font-style: normal;
    color: var(--yellow);
  }

  p {
    font-size: 18px;
    line-height: 1.7;
    max-width: 560px;
    color: rgba(241, 240, 226, 0.86);
  }

  @media (max-width: 960px) {
    h1 {
      font-size: 40px;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

export const Botoes = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin-top: 32px;
`;

export const BotaoPrincipal = styled.button`
  background: var(--yellow);
  color: var(--dark);
  border: none;
  padding: 16px 34px;
  border-radius: 18px;
  font-weight: 900;
  font-size: 16px;
  cursor: pointer;
  box-shadow: 0 6px 0 #b8840a;
  transition: transform 0.1s, box-shadow 0.1s;

  &:hover {
    filter: brightness(1.05);
  }

  /* efeito de botão de jogo: "afunda" ao clicar */
  &:active {
    transform: translateY(4px);
    box-shadow: 0 2px 0 #b8840a;
  }
`;

export const BotaoSecundario = styled.button`
  background: transparent;
  color: var(--cream);
  border: 2px solid rgba(241, 240, 226, 0.7);
  padding: 14px 30px;
  border-radius: 18px;
  font-weight: 800;
  font-size: 16px;
  cursor: pointer;

  &:hover {
    background: rgba(241, 240, 226, 0.12);
  }
`;

export const Destaques = styled.ul`
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-top: 30px;
  list-style: none;

  li {
    font-size: 14px;
    color: rgba(241, 240, 226, 0.8);
  }

  strong {
    color: var(--cream);
    font-size: 20px;
    display: block;
  }
`;

/* lado direito: cartão de código + mascote + selos de jogo */
export const Cena = styled.div`
  position: relative;
  z-index: 2;
  min-height: 420px;

  @media (max-width: 960px) {
    min-height: 360px;
  }
`;

export const CartaoCodigo = styled.pre`
  position: absolute;
  top: 30px;
  left: 0;
  right: 90px; /* espaço à direita para o mascote não cobrir o código */
  background: #0b0b2e;
  border: 2px solid rgba(241, 240, 226, 0.15);
  border-radius: 22px;
  padding: 44px 24px 24px;
  color: var(--cream);
  font-family: "Courier New", monospace;
  font-size: 15px;
  line-height: 1.7;
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.45);
  transform: rotate(-3deg);
  overflow: hidden;
  white-space: pre;

  /* as três "bolinhas" de janela de editor */
  &::before {
    content: "● ● ●";
    position: absolute;
    top: 12px;
    left: 18px;
    font-size: 12px;
    letter-spacing: 2px;
    color: var(--orange);
  }

  .kw { color: var(--orange); font-weight: 700; }
  .ty { color: var(--yellow); }
  .st { color: #7fd1c7; }
  .cm { color: rgba(241, 240, 226, 0.45); }

  @media (max-width: 600px) {
    font-size: 12px;
    right: 0;
  }
`;

export const Mascote = styled.img`
  position: absolute;
  right: -10px;
  bottom: -30px;
  width: 175px;
  height: auto;
  filter: drop-shadow(0 18px 24px rgba(0, 0, 0, 0.45));
  animation: ${flutuar} 3.2s ease-in-out infinite;

  @media (max-width: 600px) {
    width: 130px;
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

export const Selo = styled.span`
  position: absolute;
  background: var(--cream);
  color: var(--dark);
  font-weight: 900;
  font-size: 14px;
  padding: 8px 14px;
  border-radius: 999px;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
  animation: ${flutuar} 4s ease-in-out infinite;

  &.xp {
    top: 0;
    right: 30px;
    background: var(--yellow);
  }

  &.ofensiva {
    bottom: 70px;
    left: -10px;
    animation-delay: 1s;
  }

  &.fase {
    bottom: 10px;
    left: 34%;
    background: var(--green);
    color: var(--cream);
    animation-delay: 2s;
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;
