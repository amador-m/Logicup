import styled from "styled-components";

export const Rotulo = styled.label`
  display: flex;
  align-items: flex-start;
  gap: 12px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--dark);
  cursor: pointer;

  input {
    flex-shrink: 0;
    width: 20px;
    height: 20px;
    margin-top: 1px;
    accent-color: var(--green);
    cursor: pointer;
  }

  input:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }

  a {
    color: var(--green);
    font-weight: 700;
    text-decoration: underline;
    text-underline-offset: 3px;
  }

  a:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
    border-radius: 4px;
  }

  /* aviso só para leitores de tela: quem enxerga já percebe a nova aba ao clicar */
  .nova-aba {
    position: absolute;
    width: 1px;
    height: 1px;
    overflow: hidden;
    clip-path: inset(50%);
    white-space: nowrap;
  }
`;

export const Erro = styled.p`
  margin-top: 6px;
  margin-left: 32px;
  color: var(--orange);
  font-size: 13px;
  font-weight: 600;
`;
