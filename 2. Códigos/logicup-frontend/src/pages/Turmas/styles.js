import styled from "styled-components";

/*
 * Sprint 9: estilos das telas de turma (professor e aluno).
 * Reaproveitam o fundo e a faixa de título da trilha, o quadro da ementa e os avisos do ranking,
 * para as telas novas parecerem parte do mesmo jogo.
 */
export { Page, Hero } from "../LearningPath/styles";
export { Quadro } from "../Teacher/styles";
export { Aviso, BotaoAcao, Lista } from "../Ranking/styles";

export const Secao = styled.section`
  & + & {
    margin-top: 28px;
  }

  > h2 {
    font-size: 20px;
    color: var(--brown-dark);
    margin-bottom: 12px;
  }

  > p.ajuda {
    color: var(--brown-dark);
    font-size: 14px;
    line-height: 1.6;
    margin: -4px 0 14px;
  }
`;

/* campo + botão na mesma linha (empilha no celular) */
export const Formulario = styled.form`
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: flex-end;

  label {
    display: flex;
    flex-direction: column;
    gap: 6px;
    flex: 1 1 220px;
    font-weight: 700;
    font-size: 14px;
    color: var(--dark);
  }

  input, select {
    padding: 13px 14px;
    border-radius: 14px;
    border: 2px solid rgba(236, 172, 12, 0.35);
    background: var(--white);
    font-size: 15px;
    font-family: inherit;
    min-width: 0;
  }

  input:focus-visible, select:focus-visible {
    outline: none;
    border-color: var(--yellow);
  }

  button {
    padding: 13px 22px;
  }

  @media (max-width: 600px) {
    button {
      width: 100%;
    }
  }
`;

export const ErroCampo = styled.p`
  margin-top: 8px;
  color: var(--orange);
  font-weight: 700;
  font-size: 14px;
`;

export const ListaTurmas = styled.ul`
  list-style: none;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
`;

export const CartaoTurma = styled.li`
  background: var(--white);
  border: 2px solid rgba(236, 172, 12, 0.35);
  border-radius: 22px;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;

  h3 {
    font-size: 18px;
    overflow-wrap: anywhere;
  }

  .info {
    color: var(--brown-dark);
    font-size: 14px;
  }

  a {
    margin-top: 6px;
    align-self: flex-start;
    font-weight: 800;
    color: var(--green);
    text-decoration: underline;
    text-underline-offset: 3px;
  }
`;

/* o código de convite em destaque, com copiar e gerar outro */
export const CodigoConvite = styled.div`
  background: rgba(241, 240, 226, 0.1);
  border: 1px solid rgba(241, 240, 226, 0.2);
  border-radius: 24px;
  padding: 18px 22px;
  text-align: center;
  min-width: 260px;

  > span {
    display: block;
    font-size: 13px;
  }

  code {
    display: block;
    margin: 6px 0 12px;
    font-size: 34px;
    font-weight: 900;
    letter-spacing: 6px;
    color: var(--cream);
    font-family: "Courier New", monospace;
  }

  .acoes {
    display: flex;
    gap: 8px;
    justify-content: center;
    flex-wrap: wrap;
  }

  @media (max-width: 600px) {
    width: 100%;
    min-width: 0;

    code {
      font-size: 28px;
      letter-spacing: 4px;
    }
  }
`;

export const BotaoPequeno = styled.button`
  padding: 8px 14px;
  border-radius: 12px;
  border: 2px solid var(--yellow);
  background: transparent;
  color: var(--yellow);
  font-weight: 800;
  font-size: 13px;
  cursor: pointer;

  &:hover:not(:disabled) {
    background: var(--yellow);
    color: var(--dark);
  }

  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }

  &:focus-visible {
    outline: 3px solid var(--cream);
    outline-offset: 2px;
  }

  /* versão para fundo claro (dentro do quadro) */
  &.claro {
    border-color: var(--brown-dark);
    color: var(--brown-dark);
  }

  &.claro:hover:not(:disabled) {
    background: var(--brown-dark);
    color: var(--cream);
  }

  &.perigo {
    border-color: var(--orange);
    color: var(--orange);
  }

  &.perigo:hover:not(:disabled) {
    background: var(--orange);
    color: var(--white);
  }
`;

/* abas: Alunos | Tarefas | Ranking | Configurações */
export const Abas = styled.div`
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  border-bottom: 2px solid rgba(236, 172, 12, 0.3);

  button {
    padding: 10px 16px;
    border: none;
    background: transparent;
    font-weight: 800;
    font-size: 15px;
    color: var(--brown-dark);
    border-bottom: 4px solid transparent;
    margin-bottom: -2px;
    cursor: pointer;
  }

  button[aria-selected="true"] {
    color: var(--dark);
    border-bottom-color: var(--orange);
  }

  button:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
    border-radius: 8px;
  }
`;

/* tabela que rola só ela no celular */
export const TabelaRolavel = styled.div`
  overflow-x: auto;
  border-radius: 18px;
  border: 2px solid rgba(236, 172, 12, 0.3);
  background: var(--white);

  table {
    width: 100%;
    min-width: 720px;
    border-collapse: collapse;
    font-size: 14px;
  }

  th, td {
    text-align: left;
    padding: 12px 14px;
    vertical-align: middle;
  }

  th {
    background: rgba(236, 172, 12, 0.18);
    color: var(--brown-dark);
    font-size: 13px;
  }

  tbody tr + tr td {
    border-top: 1px solid rgba(4, 4, 35, 0.08);
  }

  td.numero {
    font-weight: 800;
  }

  td small {
    display: block;
    color: var(--brown-dark);
  }

  /* Sprint 9: avatar + nome do aluno */
  td .aluno {
    display: flex;
    align-items: center;
    gap: 10px;
  }
`;

export const ListaTarefas = styled.ul`
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 10px;
`;

export const ItemTarefa = styled.li`
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  background: var(--white);
  border: 2px solid rgba(236, 172, 12, 0.3);
  border-radius: 18px;
  padding: 14px 16px;

  .texto {
    flex: 1 1 220px;
    min-width: 0;
  }

  .fase {
    display: block;
    font-size: 12px;
    font-weight: 800;
    color: var(--brown-dark);
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  h3 {
    font-size: 16px;
    overflow-wrap: anywhere;
  }

  .detalhe {
    font-size: 13px;
    color: var(--brown-dark);
  }

  &.concluida {
    border-color: rgba(40, 128, 120, 0.45);
  }

  &.bloqueada {
    opacity: 0.75;
  }
`;

/* selo de status (o TEXTO diz o status; a cor só reforça) */
export const Selo = styled.span`
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
  background: rgba(236, 172, 12, 0.2);
  color: var(--brown-dark);

  &.CONCLUIDA {
    background: var(--green);
    color: var(--white);
  }

  &.BLOQUEADA {
    background: rgba(4, 4, 35, 0.1);
    color: var(--dark);
  }

  &.atrasada {
    background: var(--orange);
    color: var(--white);
  }
`;

export const Mascote = styled.img`
  display: block;
  width: 110px;
  margin: 0 auto 12px;
`;

export const LinhaAcoes = styled.div`
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 8px;
`;
