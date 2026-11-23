import styled from "styled-components";

/* Sprint 11: a área do admin usa o mesmo fundo, faixa de título e quadro creme da ementa e das turmas */
export { Page, Hero } from "../LearningPath/styles";
export { Quadro } from "../Teacher/styles";
export { Abas, TabelaRolavel, BotaoPequeno, Selo } from "../Turmas/styles";

/* busca + filtro de perfil (empilha no celular) */
export const Filtros = styled.form`
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;

  input,
  select {
    max-width: 100%; /* Sprint 11: no iPhone, o <select> não estica além da tela */
    min-width: 0;
    padding: 11px 14px;
    border-radius: 12px;
    border: 2px solid rgba(118, 73, 53, 0.3);
    background: var(--white);
    font-size: 15px;
  }

  input {
    flex: 1;
    min-width: 220px;
  }

  input:focus-visible,
  select:focus-visible {
    outline: none;
    border-color: var(--yellow);
  }

  label {
    position: absolute;
    width: 1px;
    height: 1px;
    overflow: hidden;
    clip: rect(0 0 0 0);
  }
`;

export const Paginacao = styled.nav`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  margin-top: 16px;
  font-weight: 700;
  color: var(--brown-dark);
`;

export const Info = styled.p`
  color: var(--brown-dark);
  font-size: 14px;
  margin-bottom: 10px;

  &.erro {
    color: var(--orange);
    font-weight: 700;
  }
`;

/* ---------- aba Conteúdo ---------- */

export const Aviso = styled.p`
  background: rgba(236, 172, 12, 0.18);
  border-left: 6px solid var(--yellow);
  border-radius: 12px;
  padding: 12px 14px;
  color: var(--brown-dark);
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
`;

export const Editor = styled.form`
  display: flex;
  flex-direction: column;
  gap: 14px;

  fieldset {
    border: 2px solid rgba(236, 172, 12, 0.35);
    border-radius: 18px;
    padding: 14px 16px 16px;
    display: flex;
    flex-direction: column;
    gap: 10px;
    background: var(--white);
  }

  legend {
    font-weight: 900;
    color: var(--dark);
    padding: 0 6px;
  }

  label {
    display: flex;
    flex-direction: column;
    gap: 6px;
    font-size: 13px;
    font-weight: 800;
    color: var(--brown-dark);
  }

  input,
  textarea {
    padding: 10px 12px;
    border-radius: 10px;
    border: 2px solid rgba(118, 73, 53, 0.25);
    font: inherit;
    font-size: 15px;
    font-weight: 400;
    color: var(--dark);
    background: var(--cream);
    resize: vertical;
  }

  textarea.codigo {
    font-family: "JetBrains Mono", "Consolas", monospace;
    font-size: 13px;
    background: #1e1e2e;
    color: #f1f0e2;
  }

  input:focus-visible,
  textarea:focus-visible {
    outline: none;
    border-color: var(--yellow);
  }

  .tabela {
    display: grid;
    gap: 6px;
  }

  .acoes {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }

  .acoes .ok {
    color: var(--green);
    font-weight: 800;
    font-size: 14px;
  }

  .acoes .erro {
    color: var(--orange);
    font-weight: 800;
    font-size: 14px;
  }
`;

export const Exercicio = styled.details`
  border: 2px solid rgba(236, 172, 12, 0.35);
  border-radius: 18px;
  background: var(--white);

  summary {
    cursor: pointer;
    padding: 12px 16px;
    font-weight: 800;
    color: var(--dark);
  }

  summary small {
    color: var(--brown-dark);
    font-weight: 700;
    margin-left: 6px;
  }

  > form {
    padding: 0 16px 16px;
  }
`;
