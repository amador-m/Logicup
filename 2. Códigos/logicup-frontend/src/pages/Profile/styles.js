import styled from "styled-components";

/*
 * Mesma faixa de título e cartões de número da trilha: o perfil faz parte do mesmo "jogo".
 * Sprint 7: a versão para celular (empilhar texto e avatar) agora fica na própria faixa
 * compartilhada (LearningPath/styles), então o perfil não precisa mais de ajuste local.
 */
export { Page, Hero, StatCard } from "../LearningPath/styles";

export const Cartao = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;

  small {
    font-size: 13px;
    color: rgba(241, 240, 226, 0.7);
  }
`;

/* Lugar da futura foto de perfil */
export const AvatarGrande = styled.div`
  width: 132px;
  height: 132px;
  border-radius: 50%;
  background: var(--yellow);
  color: var(--dark);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 900;
  font-size: 44px;
  border: 7px solid var(--cream);
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.3);
`;

export const Stats = styled.section`
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px;
  padding: 0 24px 30px;

  > div {
    min-width: 130px;
  }
`;

export const Paineis = styled.div`
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  align-items: start;

  @media (max-width: 820px) {
    grid-template-columns: minmax(0, 1fr);
    padding: 0 16px;
  }
`;

export const Painel = styled.section`
  background: var(--cream);
  border: 5px solid var(--yellow);
  border-radius: 32px;
  padding: 30px;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);

  h2 {
    color: var(--dark);
    font-size: 26px;
    margin-bottom: 20px;
  }

  input:disabled {
    background: rgba(255, 255, 255, 0.5);
    color: var(--brown-dark);
    cursor: not-allowed;
  }

  @media (max-width: 600px) {
    padding: 22px 18px;
  }
`;

export const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 18px;

  button:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

export const Sucesso = styled.p`
  color: var(--green);
  font-weight: 700;
  font-size: 14px;
`;

/* ---------- Sprint 7: progresso por fase ---------- */

/* mesmo card creme com borda amarela dos painéis, ocupando a largura dos dois */
export const SecaoFases = styled.section`
  max-width: 1000px;
  margin: 0 auto 24px;
  background: var(--cream);
  border: 5px solid var(--yellow);
  border-radius: 32px;
  padding: 30px;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);

  h2 {
    color: var(--dark);
    font-size: 26px;
    margin-bottom: 20px;
  }

  @media (max-width: 1048px) {
    margin: 0 24px 24px;
  }

  @media (max-width: 820px) {
    margin: 0 16px 24px;
  }

  @media (max-width: 600px) {
    padding: 22px 18px;
  }
`;

/* 2 colunas (5 fases em cada) em telas largas; 1 coluna abaixo de 820px */
export const ListaFases = styled.ol`
  list-style: none;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  grid-auto-flow: column;
  grid-template-rows: repeat(5, auto);
  gap: 12px 24px;

  @media (max-width: 820px) {
    grid-template-columns: minmax(0, 1fr);
    grid-auto-flow: row;
    grid-template-rows: none;
  }
`;

export const LinhaFase = styled.li`
  display: flex;
  align-items: center;
  gap: 14px;
  background: var(--white);
  border: 2px solid rgba(236, 172, 12, 0.3);
  border-radius: 20px;
  padding: 10px 16px 12px 10px;

  img {
    width: 56px;
    height: 56px;
    flex-shrink: 0;
    object-fit: contain;
  }

  .info {
    flex: 1;
    min-width: 0; /* deixa o título quebrar em vez de empurrar a linha para fora */
  }

  .topo {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
  }

  .numero {
    font-size: 12px;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: 0.06em;
    color: var(--orange);
  }

  h3 {
    font-size: 15px;
    color: var(--dark);
    margin: 2px 0 6px;
    overflow-wrap: anywhere;
  }

  .numeros {
    display: block;
    margin-top: 5px;
    font-size: 12px;
    font-weight: 700;
    color: var(--green);
  }

  /* o XP não quebra no meio ("187/205" numa linha e "XP" na outra) */
  .numeros .xp {
    white-space: nowrap;
  }

  .selo {
    flex-shrink: 0;
    font-size: 11px;
    font-weight: 800;
    padding: 2px 10px;
    border-radius: 999px;
    background: rgba(118, 73, 53, 0.14);
    color: var(--brown-dark);
  }

  .selo.concluida {
    background: var(--green);
    color: var(--cream);
  }

  .selo.andamento,
  .selo.disponivel {
    background: rgba(236, 172, 12, 0.25);
    color: var(--brown-dark);
  }

  /* fase concluída: mesma borda verde de destaque do ranking */
  &.completed {
    border-color: rgba(40, 128, 120, 0.45);
  }

  &.locked,
  &.soon {
    background: rgba(255, 255, 255, 0.55);

    h3,
    .numeros {
      color: var(--brown-light);
    }
  }
`;

export const Barra = styled.div`
  height: 10px;
  border-radius: 999px;
  background: rgba(118, 73, 53, 0.14);
  overflow: hidden;

  span {
    display: block;
    height: 100%;
    border-radius: inherit;
    background: linear-gradient(90deg, var(--yellow), var(--green));
    transition: width 0.4s ease-out;
  }

  @media (prefers-reduced-motion: reduce) {
    span {
      transition: none;
    }
  }
`;
