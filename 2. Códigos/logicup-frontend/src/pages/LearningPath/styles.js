import styled, { keyframes } from "styled-components";

export const Page = styled.main`
    min-height: 100vh;
    background:
            radial-gradient(circle at 20% 15%, rgba(236, 172, 12, 0.18), transparent 24%),
            radial-gradient(circle at 85% 35%, rgba(227, 92, 51, 0.18), transparent 26%),
            radial-gradient(circle at 18% 80%, rgba(40, 128, 120, 0.24), transparent 28%),
            linear-gradient(180deg, var(--dark) 0%, var(--green) 100%);
    padding-bottom: 80px;
`;

export const Hero = styled.section`
  padding: 70px 120px 40px;
  color: var(--cream);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 40px;

  > div:first-child {
    max-width: 680px;
  }

  span {
    color: var(--yellow);
    font-weight: 800;
    text-transform: uppercase;
    letter-spacing: 1px;
  }

  h1 {
    font-size: 54px;
    margin: 14px 0;
    line-height: 1.05;
  }

  p {
    max-width: 650px;
    color: rgba(241, 240, 226, 0.82);
    line-height: 1.7;
    font-size: 17px;
  }

  /*
   * Sprint 7 (responsividade): abaixo de 900px o texto e o cartão ao lado EMPILHAM.
   * Vale para a trilha, o ranking e o perfil, que usam esta mesma faixa de título.
   */
  @media (max-width: 900px) {
    flex-direction: column;
    align-items: flex-start;
    padding: 40px 20px 28px;
    gap: 24px;

    h1 {
      font-size: 34px;
      overflow-wrap: anywhere;
    }

    p {
      font-size: 16px;
    }
  }
`;

export const UserInfo = styled.div`
  background: rgba(241, 240, 226, 0.1);
  border: 1px solid rgba(241, 240, 226, 0.18);
  border-radius: 28px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 18px;
  backdrop-filter: blur(10px);

  /* celular: avatar e números podem ir para a linha de baixo */
  @media (max-width: 900px) {
    flex-wrap: wrap;
    padding: 18px;
    gap: 14px;
  }
`;

export const Avatar = styled.div`
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--yellow);
  color: var(--dark);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 22px;
  border: 5px solid var(--cream);
`;

export const Stats = styled.div`
  display: flex;
  gap: 12px;

  @media (max-width: 900px) {
    gap: 8px;
  }
`;

export const StatCard = styled.div`
  min-width: 82px;
  background: var(--cream);
  color: var(--dark);
  border-radius: 18px;
  padding: 14px;
  text-align: center;

  strong {
    display: block;
    font-size: 24px;
    color: var(--orange);
  }

  span {
    font-size: 12px;
    color: var(--brown-dark);
    font-weight: 700;
    text-transform: none;
    letter-spacing: 0;
  }
`;

/* T3: no lugar dos stats, para professor/admin */
export const ViewModeInfo = styled.div`
  max-width: 220px;
  color: var(--cream);

  strong {
    display: block;
    font-size: 18px;
    color: var(--yellow);
    margin-bottom: 4px;
  }

  span {
    font-size: 13px;
    line-height: 1.5;
    color: rgba(241, 240, 226, 0.85);
    font-weight: 500;
    text-transform: none;
    letter-spacing: 0;
  }
`;

/* carregando / erro dentro do mapa */
export const StatusMessage = styled.div`
  position: relative;
  z-index: 2;
  text-align: center;
  color: var(--dark);
  font-weight: 700;
  font-size: 17px;
  padding: 40px 12px;

  &.error p {
    color: var(--orange);
    margin-bottom: 18px;
  }

  button {
    max-width: 240px;
  }
`;

export const ActionButton = styled.button`
  width: 100%;
  background: var(--green);
  color: var(--white);
  border: none;
  padding: 13px;
  border-radius: 16px;
  font-weight: 800;
  cursor: pointer;
  transition: 0.3s;

  &:hover:not(:disabled) {
    background: var(--dark);
  }

  &:disabled {
    background: var(--brown-light);
    cursor: not-allowed;
  }
`;

/* ====================== Sprint 7: mapa estilo jogo ====================== */

/* "Tabuleiro" creme com borda amarela (identidade do LogicUp) onde fica o caminho das fases */
export const Mapa = styled.section`
  position: relative;
  max-width: 780px;
  margin: 30px auto 0;
  padding: 50px 24px 30px;
  border-radius: 42px;
  border: 5px solid var(--yellow);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
  overflow: hidden;
  background:
    radial-gradient(circle at 12% 18%, rgba(236, 172, 12, 0.22), transparent 22%),
    radial-gradient(circle at 88% 42%, rgba(227, 92, 51, 0.16), transparent 24%),
    radial-gradient(circle at 15% 78%, rgba(40, 128, 120, 0.18), transparent 24%),
    var(--cream);

  @media (max-width: 820px) {
    margin: 24px 16px 0;
    padding: 40px 8px 24px;
  }
`;

/* área com altura fixa: cada fase ocupa uma "faixa" vertical; os nós são posicionados por cima do SVG */
export const Trilha = styled.div`
  position: relative;
  width: 100%;
`;

/* o caminho sinuoso (SVG atrás dos nós) */
export const Caminho = styled.svg`
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  overflow: visible;
  pointer-events: none;

  path {
    fill: none;
    stroke-linecap: round;
    /* mantém a espessura da linha mesmo com o SVG esticado (preserveAspectRatio="none") */
    vector-effect: non-scaling-stroke;
  }

  .base {
    stroke: rgba(118, 73, 53, 0.25);
    stroke-width: 14px;
    stroke-dasharray: 2 22;
  }

  .percorrido {
    stroke: var(--green);
    stroke-width: 14px;
  }
`;

/* palavras de Java "flutuando" ao fundo, bem discretas */
export const Decoracao = styled.span`
  position: absolute;
  font-family: "Courier New", monospace;
  font-weight: 700;
  font-size: 22px;
  color: rgba(118, 73, 53, 0.18);
  white-space: nowrap;
  pointer-events: none;
  user-select: none;

  /* no celular não sobra espaço ao lado dos nós: as decorações somem */
  @media (max-width: 600px) {
    display: none;
  }
`;

const pulsar = keyframes`
  0%, 100% { transform: scale(1); }
  50%      { transform: scale(1.06); }
`;

const anel = keyframes`
  0%   { transform: scale(0.85); opacity: 0.7; }
  100% { transform: scale(1.35); opacity: 0; }
`;

const flutuar = keyframes`
  0%, 100% { transform: translateY(0); }
  50%      { transform: translateY(-8px); }
`;

/* um nó do mapa (ícone + textos), centralizado no ponto do caminho */
export const No = styled.div`
  position: absolute;
  transform: translateX(-50%);
  width: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  z-index: 2;

  @media (max-width: 600px) {
    width: 150px;
  }
`;

/*
 * O botão é o próprio ícone do Davi. Há duas imagens empilhadas: a normal e a "press".
 * No hover/foco/clique a normal some e a "press" aparece (efeito de botão de jogo afundando).
 */
export const BotaoFase = styled.button`
  position: relative;
  width: 132px;
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
  border-radius: 50%;

  img {
    display: block;
    width: 100%;
    height: auto;
    filter: drop-shadow(0 8px 10px rgba(4, 4, 35, 0.25));
  }

  img.press {
    position: absolute;
    inset: 0;
    opacity: 0;
  }

  &:not(:disabled):hover img.normal,
  &:not(:disabled):focus-visible img.normal,
  &:not(:disabled):active img.normal {
    opacity: 0;
  }

  &:not(:disabled):hover img.press,
  &:not(:disabled):focus-visible img.press,
  &:not(:disabled):active img.press {
    opacity: 1;
  }

  &:focus-visible {
    outline: 4px solid var(--orange);
    outline-offset: 4px;
  }

  &:disabled {
    cursor: not-allowed;
  }

  &.soon img {
    opacity: 0.55;
  }

  &.boss {
    width: 160px;
  }

  /* fase atual: pulsa e tem um anel se expandindo em volta */
  &.atual {
    animation: ${pulsar} 1.8s ease-in-out infinite;
  }

  &.atual::before {
    content: "";
    position: absolute;
    left: 8%;
    right: 8%;
    top: 12%;
    bottom: 6%;
    border-radius: 50%;
    border: 4px solid var(--yellow);
    animation: ${anel} 1.8s ease-out infinite;
  }

  @media (max-width: 600px) {
    width: 104px;

    &.boss {
      width: 124px;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    &.atual,
    &.atual::before {
      animation: none;
    }
  }
`;

export const Rotulo = styled.div`
  margin-top: 6px;
  line-height: 1.3;
  /* fundo creme: o caminho tracejado passa POR TRÁS do texto sem atrapalhar a leitura */
  background: rgba(241, 240, 226, 0.94);
  border-radius: 14px;
  padding: 4px 10px 6px;
  box-shadow: 0 0 0 3px rgba(241, 240, 226, 0.94);

  .numero {
    display: inline-block;
    font-size: 12px;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: 0.06em;
    color: var(--orange);
  }

  h2 {
    font-size: 16px;
    color: var(--dark);
    margin: 2px 0 4px;
  }

  .progresso {
    font-size: 12px;
    font-weight: 700;
    color: var(--green);
  }

  .selo {
    display: inline-block;
    margin-top: 2px;
    font-size: 11px;
    font-weight: 800;
    padding: 2px 10px;
    border-radius: 999px;
    background: rgba(118, 73, 53, 0.14);
    color: var(--brown-dark);
  }

  &.locked h2,
  &.soon h2 {
    color: var(--brown-light);
  }

  @media (max-width: 600px) {
    h2 {
      font-size: 14px;
    }
  }
`;

/* mascote ao lado da fase atual, com balão chamando para continuar */
export const MascoteAtual = styled.div`
  position: absolute;
  top: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 3;
  pointer-events: none;

  &.direita {
    left: calc(50% + 78px);
  }

  &.esquerda {
    right: calc(50% + 78px);
    flex-direction: row-reverse;
  }

  img {
    width: 78px;
    height: auto;
    animation: ${flutuar} 3s ease-in-out infinite;
  }

  span {
    background: var(--white);
    border: 3px solid var(--yellow);
    color: var(--dark);
    font-weight: 900;
    font-size: 13px;
    padding: 6px 10px;
    border-radius: 14px;
    white-space: nowrap;
  }

  @media (max-width: 600px) {
    &.direita {
      left: calc(50% + 56px);
    }

    &.esquerda {
      right: calc(50% + 56px);
    }

    img {
      width: 56px;
    }

    span {
      display: none;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    img {
      animation: none;
    }
  }
`;
