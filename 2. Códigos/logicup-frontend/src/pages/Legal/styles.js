import styled from "styled-components";

/*
 * Sprint 8: páginas legais. Mesmo fundo e faixa de título da trilha/ranking (Page e Hero),
 * com o texto num cartão claro e largura de leitura confortável (~70 caracteres por linha).
 */
export const Documento = styled.article`
  max-width: 820px;
  margin: 10px auto 0;
  background: var(--cream);
  border: 5px solid var(--yellow);
  border-radius: 36px;
  padding: 40px 48px;
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
  line-height: 1.7;
  color: var(--dark);

  a {
    color: var(--green);
    font-weight: 600;
    text-decoration: underline;
    text-underline-offset: 3px;
  }

  a:focus-visible {
    outline: 3px solid var(--orange);
    outline-offset: 2px;
    border-radius: 4px;
  }

  @media (max-width: 600px) {
    margin: 10px 16px 0;
    padding: 24px 20px;
    border-radius: 28px;
  }
`;

export const Meta = styled.p`
  font-size: 14px;
  font-weight: 600;
  color: var(--brown-dark);
`;

export const AvisoPreliminar = styled.p`
  margin: 16px 0 24px;
  padding: 14px 18px;
  border-left: 5px solid var(--orange);
  border-radius: 12px;
  background: rgba(227, 92, 51, 0.1);
  font-size: 15px;
`;

export const Sumario = styled.nav`
  padding: 18px 22px;
  border-radius: 20px;
  background: var(--white);
  border: 2px solid rgba(236, 172, 12, 0.3);

  h2 {
    font-size: 16px;
    margin-bottom: 8px;
  }

  ol {
    padding-left: 22px;
    columns: 2;
    column-gap: 32px;
    font-size: 15px;
  }

  li {
    break-inside: avoid;
    list-style: decimal; /* o Global tira os marcadores de ul/nav; aqui a numeração ajuda a se localizar */
  }

  @media (max-width: 600px) {
    ol {
      columns: 1;
    }
  }
`;

export const Secao = styled.section`
  margin-top: 32px;
  scroll-margin-top: 24px; /* respiro acima do título quando se chega pelo link do sumário */

  h2 {
    font-size: 22px;
    line-height: 1.3;
    margin-bottom: 10px;
    color: var(--brown-dark);
  }

  p + p,
  ul + p,
  p + ul,
  p + div,
  div + p {
    margin-top: 10px;
  }

  ul {
    padding-left: 22px;
  }

  li {
    list-style: disc;
    margin-top: 4px;
  }
`;

export const TabelaRolavel = styled.div`
  overflow-x: auto;
  border-radius: 16px;
  border: 2px solid rgba(236, 172, 12, 0.3);
  background: var(--white);

  &:focus-visible {
    outline: 3px solid var(--orange);
    outline-offset: 2px;
  }

  table {
    width: 100%;
    min-width: 560px;
    border-collapse: collapse;
    font-size: 14px;
    line-height: 1.5;
  }

  th,
  td {
    text-align: left;
    vertical-align: top;
    padding: 10px 14px;
  }

  th {
    background: rgba(236, 172, 12, 0.18);
    color: var(--brown-dark);
  }

  tbody tr + tr td {
    border-top: 1px solid rgba(4, 4, 35, 0.08);
  }

  td:first-child {
    font-weight: 600;
    min-width: 12em; /* nomes longos de cookie (ex.: __Host-LOGICUP_TOKEN) cabem sem quebrar no meio */
    overflow-wrap: anywhere;
  }
`;

export const OutrosDocumentos = styled.nav`
  margin-top: 40px;
  padding-top: 20px;
  border-top: 2px solid rgba(236, 172, 12, 0.3);
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;
  font-size: 15px;

  span {
    font-weight: 600;
  }
`;
