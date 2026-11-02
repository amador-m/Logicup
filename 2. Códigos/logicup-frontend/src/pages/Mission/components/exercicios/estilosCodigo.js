import styled from "styled-components";

/* Sprint 9 (ADR-001): estilos das questões de CÓDIGO (editor, Executar, resultado dos testes). */

export const AcoesEditor = styled.div`
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 14px;
  margin: 4px 0 12px;

  .exemplos {
    font-size: 13px;
    color: var(--brown-dark);
  }
`;

export const BotaoExecutar = styled.button`
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: var(--green);
  color: var(--white);
  border: none;
  border-radius: 14px;
  padding: 10px 18px;
  font-weight: 800;
  cursor: pointer;

  &:hover:not(:disabled) {
    filter: brightness(1.08);
  }

  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;

/* Faixa com a situação da execução (verde = rodou, laranja = erro) */
export const Situacao = styled.p`
  margin: 8px 0 0;
  padding: 10px 14px;
  border-radius: 12px;
  font-weight: 700;
  line-height: 1.5;

  &.ok {
    background: rgba(40, 128, 120, 0.12);
    color: var(--green);
  }

  &.erro {
    background: rgba(227, 92, 51, 0.1);
    color: var(--orange);
  }

  small {
    font-weight: 500;
    opacity: 0.8;
    margin-left: 6px;
  }
`;

export const Dica = styled.p`
  margin: 10px 0 0;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(236, 172, 12, 0.16);
  color: var(--dark);
  line-height: 1.5;
`;

/* Barra de progresso dos casos de teste (ex.: 3 de 7) */
export const ProgressoCasos = styled.div`
  margin: 6px 0 10px;

  .texto {
    font-weight: 700;
    font-size: 14px;
  }

  .trilho {
    margin-top: 6px;
    height: 10px;
    border-radius: 999px;
    background: rgba(118, 73, 53, 0.15);
    overflow: hidden;
  }

  .preenchido {
    height: 100%;
    border-radius: 999px;
    background: var(--green);
    transition: width 0.4s ease;
  }

  @media (prefers-reduced-motion: reduce) {
    .preenchido {
      transition: none;
    }
  }
`;

export const CasoQueFalhou = styled.p`
  margin: 8px 0 0;

  code {
    font-family: "JetBrains Mono", Consolas, monospace;
    background: rgba(4, 4, 35, 0.07);
    padding: 2px 8px;
    border-radius: 6px;
  }
`;
