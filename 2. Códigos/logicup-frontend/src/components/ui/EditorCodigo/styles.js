import styled from "styled-components";

/* Área do editor dentro da "janela de IDE" (a Janela e a Barra vêm do CodigoJava). */
export const CorpoEditor = styled.div`
  min-height: 214px;

  .cm-editor {
    max-height: 60vh; /* código longo rola dentro do editor, sem empurrar a página toda */
  }
`;

export const Carregando = styled.p`
  margin: 0;
  padding: 18px;
  color: #c9ccd6;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 14px;
`;

export const AjudaTeclado = styled.p`
  margin: -8px 0 12px;
  font-size: 12px;
  color: var(--brown-dark);
  opacity: 0.85;
`;
