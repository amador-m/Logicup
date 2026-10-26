import styled from "styled-components";

/* Mesmo fundo e mesma faixa de título da trilha, do ranking e do perfil */
export { Page, Hero } from "../LearningPath/styles";
export { Aviso, BotaoAcao } from "../Ranking/styles";

/* Os 3 números do topo (fases, questões, habilidades), no lugar dos stats do aluno */
export const Resumo = styled.div`
  display: flex;
  gap: 12px;
  flex-wrap: wrap;

  > div {
    min-width: 96px;
    background: var(--cream);
    border-radius: 18px;
    padding: 14px;
    text-align: center;
  }

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

  @media (max-width: 900px) {
    gap: 8px;
  }

  /* celular: os 3 números lado a lado, do mesmo tamanho */
  @media (max-width: 600px) {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    width: 100%;

    > div {
      min-width: 0;
      padding: 12px 6px;
    }

    strong {
      font-size: 20px;
    }

    span {
      font-size: 11px;
    }
  }
`;

export const Quadro = styled.section`
  max-width: 1000px;
  margin: 10px auto 0;
  background: var(--cream);
  border: 5px solid var(--yellow);
  border-radius: 32px;
  padding: 26px;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);

  @media (max-width: 1048px) {
    margin: 10px 24px 0;
  }

  @media (max-width: 820px) {
    margin: 10px 16px 0;
  }

  @media (max-width: 600px) {
    padding: 16px;
  }
`;

export const ListaFases = styled.ol`
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

/* Cabeçalho comum aos dois cartões: ícone + número + título */
const cabecalhoDaFase = `
  img {
    width: 56px;
    height: 56px;
    flex-shrink: 0;
    object-fit: contain;
  }

  .info {
    flex: 1;
    min-width: 0;
  }

  .numero {
    font-size: 12px;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: 0.06em;
    color: var(--orange);
  }

  h3 {
    font-size: 18px;
    color: var(--dark);
    margin: 2px 0;
    overflow-wrap: anywhere;
  }
`;

export const CartaoFase = styled.details`
  background: var(--white);
  border: 2px solid rgba(236, 172, 12, 0.45);
  border-radius: 22px;
  overflow: hidden;

  &[open] {
    border-color: var(--yellow);
  }

  summary {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 12px 18px 12px 12px;
    cursor: pointer;
    list-style: none; /* tira o triângulo padrão; usamos a nossa seta */

    &::-webkit-details-marker {
      display: none;
    }

    &:hover {
      background: rgba(236, 172, 12, 0.1);
    }

    &:focus-visible {
      outline: 3px solid var(--green);
      outline-offset: -3px;
    }

    ${cabecalhoDaFase}
  }

  .contagem {
    font-size: 13px;
    font-weight: 700;
    color: var(--brown-dark);
  }

  .seta {
    font-size: 22px;
    color: var(--brown-dark);
    transition: transform 0.2s ease-out;
  }

  &[open] .seta {
    transform: rotate(180deg);
  }

  .conteudo {
    padding: 4px 20px 20px;
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .objetivo-geral {
    color: var(--dark);
    line-height: 1.6;
    background: rgba(236, 172, 12, 0.12);
    border-radius: 14px;
    padding: 12px 16px;
  }

  @media (max-width: 600px) {
    .conteudo {
      padding: 4px 12px 14px;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .seta {
      transition: none;
    }
  }
`;

export const CartaoEmBreve = styled.div`
  display: flex;
  align-items: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.55);
  border: 2px dashed rgba(118, 73, 53, 0.25);
  border-radius: 22px;
  padding: 10px 18px 10px 12px;

  ${cabecalhoDaFase}

  .numero {
    color: var(--brown-dark);
  }

  h3 {
    font-size: 16px;
    color: var(--brown-dark);
  }

  p {
    font-size: 13px;
    color: var(--brown-dark);
  }
`;

export const Rodada = styled.section`
  border-top: 2px solid rgba(118, 73, 53, 0.12);
  padding-top: 14px;

  h4 {
    font-size: 17px;
    color: var(--dark);
    margin-bottom: 10px;

    span {
      display: inline-block;
      background: var(--green);
      color: var(--white);
      font-size: 12px;
      font-weight: 900;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      border-radius: 999px;
      padding: 3px 10px;
      margin-right: 6px;
      vertical-align: middle;
    }
  }

  h5 {
    font-size: 13px;
    font-weight: 900;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    color: var(--brown-dark);
    margin-bottom: 6px;
  }

  .titulo-questoes {
    margin-top: 12px;
  }
`;

export const Objetivos = styled.div`
  ul {
    list-style: disc; /* o reset global tira os marcadores; aqui eles ajudam a separar os objetivos */
    padding-left: 20px;
    display: flex;
    flex-direction: column;
    gap: 4px;
    color: var(--dark);
    line-height: 1.5;
  }
`;

/* 2 colunas em telas largas; 1 coluna abaixo de 820px */
export const ListaQuestoes = styled.ul`
  list-style: none;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;

  @media (max-width: 820px) {
    grid-template-columns: minmax(0, 1fr);
  }
`;

export const Questao = styled.li`
  background: var(--cream);
  border: 2px solid rgba(236, 172, 12, 0.3);
  border-radius: 16px;
  padding: 10px 14px 12px;

  .topo {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 4px;
  }

  .codigo {
    font-weight: 900;
    color: var(--orange);
    font-size: 13px;
  }

  .tipo {
    font-size: 11px;
    font-weight: 800;
    background: rgba(40, 128, 120, 0.14);
    color: var(--green);
    border-radius: 999px;
    padding: 2px 8px;
  }

  .xp {
    margin-left: auto;
    font-size: 12px;
    font-weight: 800;
    color: var(--brown-dark);
  }

  h5 {
    font-size: 15px;
    text-transform: none;
    letter-spacing: 0;
    color: var(--dark);
    margin-bottom: 4px;
  }

  p {
    font-size: 14px;
    line-height: 1.5;
    color: var(--dark);
  }
`;

export const SemHabilidade = styled.p`
  && {
    font-style: italic;
    color: var(--brown-dark);
    opacity: 0.8;
  }
`;
