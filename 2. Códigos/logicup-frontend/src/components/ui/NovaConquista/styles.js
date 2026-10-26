import styled, { keyframes } from "styled-components";
import brilho from "../../../assets/icones/sparkles.png";

const aparecer = keyframes`
  from { opacity: 0; transform: translateY(24px) scale(0.92); }
  to   { opacity: 1; transform: none; }
`;

/* a moeda entra girando no próprio eixo (como uma moeda jogada) e "assenta" com um pulinho */
const girarMoeda = keyframes`
  0%   { opacity: 0; transform: perspective(600px) rotateY(-540deg) scale(0.3); }
  70%  { opacity: 1; transform: perspective(600px) rotateY(20deg) scale(1.12); }
  100% { opacity: 1; transform: perspective(600px) rotateY(0) scale(1); }
`;

/* halo dourado girando atrás da moeda (raios de luz) */
const girarHalo = keyframes`
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to   { transform: translate(-50%, -50%) rotate(360deg); }
`;

const piscar = keyframes`
  0%, 100% { opacity: 0; transform: scale(0.4) rotate(0deg); }
  50%      { opacity: 1; transform: scale(1) rotate(25deg); }
`;

const subir = keyframes`
  from { opacity: 0; transform: translateY(12px); }
  to   { opacity: 1; transform: none; }
`;

export const Dialogo = styled.dialog`
  width: min(440px, calc(100% - 32px));
  max-height: calc(100vh - 32px);
  margin: auto;
  overflow: hidden auto;
  padding: 22px 28px 24px;
  border: 5px solid var(--yellow);
  border-radius: 32px;
  background: var(--cream);
  color: var(--dark);
  text-align: center;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.4);
  animation: ${aparecer} 0.3s ease-out;

  &::backdrop {
    background: rgba(4, 4, 35, 0.7);
    backdrop-filter: blur(4px);
  }

  h2 {
    font-size: 26px;
    line-height: 1.2;
    margin: 4px 0 6px;
    outline: none;
  }

  p {
    line-height: 1.5;
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

export const Faixa = styled.span`
  display: inline-block;
  padding: 6px 16px;
  border-radius: 999px;
  background: var(--orange);
  color: var(--white);
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 1.5px;
  text-transform: uppercase;
  box-shadow: 0 4px 0 #a83a1b;
`;

export const Palco = styled.div`
  position: relative;
  height: 190px;
  margin: 8px 0 4px;

  /* raios atrás da moeda */
  &::before {
    content: "";
    position: absolute;
    top: 50%;
    left: 50%;
    width: 240px;
    height: 240px;
    border-radius: 50%;
    background: repeating-conic-gradient(rgba(236, 172, 12, 0.35) 0deg 12deg, transparent 12deg 30deg);
    -webkit-mask: radial-gradient(circle, #000 35%, transparent 70%);
    mask: radial-gradient(circle, #000 35%, transparent 70%);
    animation: ${girarHalo} 12s linear infinite;
    transform: translate(-50%, -50%);
  }

  .moeda {
    position: relative;
    width: 170px;
    height: auto;
    margin-top: 18px;
    filter: drop-shadow(0 10px 14px rgba(118, 73, 53, 0.35));
    animation: ${girarMoeda} 0.9s cubic-bezier(0.2, 0.8, 0.3, 1.2) both;
  }

  /* Gattuccino comemorando no canto */
  .mascote {
    position: absolute;
    right: -6px;
    bottom: -4px;
    width: 92px;
    animation: ${subir} 0.4s 0.6s ease-out both;
  }

  @media (max-width: 420px) {
    .mascote {
      width: 70px;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    &::before,
    .moeda,
    .mascote {
      animation: none;
    }
  }
`;

/* estrelinhas (ícone "sparkles" da equipe, pintado de amarelo pela máscara) */
export const Brilho = styled.span.attrs({ "aria-hidden": "true" })`
  position: absolute;
  width: ${({ $tamanho }) => $tamanho}px;
  height: ${({ $tamanho }) => $tamanho}px;
  top: ${({ $topo }) => $topo};
  left: ${({ $esquerda }) => $esquerda};
  background-color: var(--yellow);
  -webkit-mask: url("${brilho}") center / contain no-repeat;
  mask: url("${brilho}") center / contain no-repeat;
  animation: ${piscar} 1.6s ${({ $atraso }) => $atraso}s ease-in-out infinite both;

  @media (prefers-reduced-motion: reduce) {
    animation: none;
    opacity: 0.8;
  }
`;

export const Nivel = styled.span`
  display: inline-block;
  margin-top: 2px;
  padding: 3px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--white);
  background: ${({ $cor }) => $cor};
`;

export const Premio = styled.div`
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 14px 0 4px;
  padding: 8px 18px;
  border-radius: 16px;
  background: var(--dark);
  color: var(--yellow);
  font-size: 20px;
  font-weight: 900;
  animation: ${subir} 0.4s 0.8s ease-out both;

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

export const Contador = styled.p`
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 1px;
  color: var(--brown-dark);
  margin-top: 10px;
`;

export const Botoes = styled.div`
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;

  @media (max-width: 420px) {
    flex-direction: column-reverse;
  }
`;

export const BotaoPrincipal = styled.button`
  padding: 13px 26px;
  border: none;
  border-radius: 16px;
  background: var(--green);
  color: var(--white);
  font-size: 16px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 4px 0 #1a5550;

  &:hover {
    background: var(--dark);
    box-shadow: 0 4px 0 #000;
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;

export const BotaoTexto = styled.button`
  padding: 13px 16px;
  border: 2px solid rgba(118, 73, 53, 0.35);
  border-radius: 16px;
  background: transparent;
  color: var(--brown-dark);
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;

  &:hover {
    background: rgba(118, 73, 53, 0.1);
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;
