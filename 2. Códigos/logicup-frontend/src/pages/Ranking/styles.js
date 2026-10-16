import styled from "styled-components";

/* Mesmo fundo e mesma faixa de título da trilha, para o ranking parecer parte do mesmo "jogo" */
export { Page, Hero } from "../LearningPath/styles";

export const SuaPosicao = styled.div`
  min-width: 240px;
  background: rgba(241, 240, 226, 0.1);
  border: 1px solid rgba(241, 240, 226, 0.18);
  border-radius: 28px;
  padding: 22px 26px;
  text-align: center;
  backdrop-filter: blur(10px);

  > span {
    display: block;
    font-size: 13px;
  }

  strong {
    display: block;
    font-size: 54px;
    line-height: 1.1;
    color: var(--cream);
  }

  p {
    font-size: 14px;
    margin: 4px auto 0;
  }
`;

export const Quadro = styled.section`
  max-width: 760px;
  margin: 10px auto 0;
  background: var(--cream);
  border: 5px solid var(--yellow);
  border-radius: 36px;
  padding: 26px;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);

  @media (max-width: 600px) {
    margin: 10px 16px 0;
    padding: 16px;
  }
`;

export const Lista = styled.ol`
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
`;

export const Linha = styled.li`
  display: grid;
  grid-template-columns: 56px 46px 1fr auto auto;
  align-items: center;
  gap: 14px;
  background: var(--white);
  border: 2px solid rgba(236, 172, 12, 0.3);
  border-radius: 20px;
  padding: 12px 18px;

  &.podio-1 {
    border-color: var(--yellow);
    background: rgba(236, 172, 12, 0.14);
  }

  /* a linha de quem está logado */
  &.voce {
    border-color: var(--green);
    background: rgba(40, 128, 120, 0.12);
    box-shadow: 0 0 0 3px rgba(40, 128, 120, 0.18);
  }

  .posicao {
    font-size: 22px;
    font-weight: 900;
    color: var(--brown-dark);
    text-align: center;
  }

  .avatar {
    width: 46px;
    height: 46px;
    border-radius: 50%;
    background: var(--dark);
    color: var(--yellow);
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 900;
    text-transform: uppercase;
  }

  .nome {
    font-weight: 800;
    color: var(--dark);
    overflow-wrap: anywhere;
  }

  .nome small {
    margin-left: 8px;
    background: var(--green);
    color: var(--cream);
    border-radius: 999px;
    padding: 2px 10px;
    font-size: 12px;
  }

  .nivel {
    font-size: 13px;
    font-weight: 700;
    color: var(--brown-dark);
  }

  .xp {
    font-weight: 900;
    color: var(--orange);
    min-width: 80px;
    text-align: right;
  }

  @media (max-width: 600px) {
    grid-template-columns: 40px 38px 1fr auto;
    gap: 10px;
    padding: 10px 12px;

    .nivel {
      display: none;
    }

    .avatar {
      width: 38px;
      height: 38px;
    }
  }
`;

export const Separador = styled.li`
  text-align: center;
  color: var(--brown-dark);
  font-weight: 900;
  letter-spacing: 4px;
`;

export const Aviso = styled.div`
  text-align: center;
  color: var(--brown-dark);
  line-height: 1.6;
  padding: 18px 12px;

  &.error {
    color: var(--orange);
    font-weight: 700;
  }

  strong {
    color: var(--dark);
  }

  button {
    margin-top: 14px;
  }
`;

export const BotaoAcao = styled.button`
  background: var(--yellow);
  color: var(--dark);
  border: none;
  cursor: pointer;
  padding: 12px 24px;
  border-radius: 16px;
  font-weight: 900;

  &:hover {
    background-color: var(--green);
    color: var(--white);
  }
`;
