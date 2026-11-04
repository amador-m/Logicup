import styled from "styled-components";

/* mesma faixa de título e fundo da trilha, das conquistas e do ranking */
export { Page, Hero } from "../LearningPath/styles";
/* mesmos botões e faixas das questões de código: Executar tem a mesma cara em todo o site */
export { BotaoExecutar, Situacao, Dica } from "../Mission/components/exercicios/estilosCodigo";

/* O "caderno" claro onde fica o laboratório, por cima do fundo escuro da página */
export const Painel = styled.section`
  margin: 0 120px;
  padding: 22px 24px 26px;
  border-radius: 28px;
  background: var(--cream);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.3);

  @media (max-width: 1100px) {
    margin: 0 40px;
  }

  @media (max-width: 900px) {
    margin: 0 12px;
    padding: 16px 12px 20px;
    border-radius: 22px;
  }
`;

export const Ferramentas = styled.div`
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
  margin-bottom: 8px;

  /* o Executar é a ação principal: maior e com "degrau" de botão de jogo */
  > button:first-child {
    font-size: 16px;
    padding: 12px 22px;
    box-shadow: 0 4px 0 #1b5a54;
  }

  /* celular: Executar e Recomeçar na mesma linha; os exemplos ocupam a linha de baixo inteira */
  @media (max-width: 900px) {
    display: grid;
    grid-template-columns: 1fr auto;
    grid-template-areas: "executar recomecar" "exemplos exemplos" "salvo salvo";

    > button:first-child { grid-area: executar; justify-content: center; }
    > label { grid-area: exemplos; }
    > button:nth-of-type(2) { grid-area: recomecar; }
    > span:last-child { grid-area: salvo; }

    label select { flex: 1; max-width: none; }
  }
`;

export const BotaoSecundario = styled.button`
  padding: 10px 16px;
  border-radius: 14px;
  border: 2px solid var(--brown-dark);
  background: transparent;
  color: var(--brown-dark);
  font-weight: 800;
  cursor: pointer;

  @media (hover: hover) {
    &:hover {
      background: var(--brown-dark);
      color: var(--cream);
    }
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;

export const Seletor = styled.label`
  display: inline-flex;
  align-items: center;
  gap: 8px;

  .rotulo {
    font-weight: 700;
    color: var(--brown-dark);
  }

  select {
    padding: 10px 12px;
    border-radius: 12px;
    border: 2px solid rgba(118, 73, 53, 0.35);
    background: var(--white);
    font-family: inherit;
    font-size: 14px;
    max-width: 230px;
  }

  select:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 1px;
  }
`;

export const Salvo = styled.span`
  margin-left: auto;
  font-size: 13px;
  color: var(--green);
  font-weight: 600;

  @media (max-width: 900px) {
    margin-left: 0;
    width: 100%;
  }
`;

/* Abas: só existem no celular/tablet */
export const Abas = styled.div`
  display: none;

  @media (max-width: 900px) {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 6px;
    margin: 10px 0 4px;
    padding: 5px;
    border-radius: 16px;
    background: rgba(118, 73, 53, 0.12);
  }
`;

export const Aba = styled.button`
  padding: 10px 4px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: var(--brown-dark);
  font-weight: 800;
  font-size: 14px;
  cursor: pointer;

  &.ativa {
    background: var(--white);
    color: var(--dark);
    box-shadow: 0 3px 8px rgba(4, 4, 35, 0.12);
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
  }
`;

export const Avisos = styled.div`
  display: grid;
  gap: 6px;
  margin: 8px 0 0;
`;

export const Aviso = styled.p`
  padding: 8px 12px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.5;
  background: rgba(4, 4, 35, 0.06);

  &.atencao {
    background: rgba(227, 92, 51, 0.12);
    color: #a83a1b;
    font-weight: 600;
  }
`;

/*
 * Desktop: blocos à esquerda (maior), Java + saída à direita.
 * minmax(0, ...) deixa a coluna ficar mais estreita que a linha de código mais longa (ela rola dentro da
 * janela de código, em vez de empurrar a página para o lado — mesma correção do feedback das questões).
 */
export const Grade = styled.div`
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(0, 1fr);
  gap: 18px;
  align-items: start;

  @media (max-width: 900px) {
    grid-template-columns: minmax(0, 1fr);
    gap: 0;

    .col-blocos, .col-java, .col-saida { display: none; }
    &[data-aba="blocos"] .col-blocos,
    &[data-aba="java"] .col-java,
    &[data-aba="saida"] .col-saida { display: block; }
    &[data-aba="java"] .col-codigo,
    &[data-aba="saida"] .col-codigo { display: block; }
    &[data-aba="blocos"] .col-codigo { display: none; }
  }
`;

export const Coluna = styled.div`
  min-width: 0;

  .col-saida {
    margin-top: 4px;
  }
`;

export const ConsoleVazio = styled.p`
  padding: 18px;
  border-radius: 14px;
  border: 2px dashed rgba(118, 73, 53, 0.3);
  color: var(--brown-dark);
  text-align: center;
  line-height: 1.6;
`;
