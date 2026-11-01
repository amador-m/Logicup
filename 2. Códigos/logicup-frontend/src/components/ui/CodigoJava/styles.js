import styled from "styled-components";

/*
 * Cores inspiradas no tema escuro das IDEs (IntelliJ "Darcula" / VS Code "Dark+"), com contraste
 * de pelo menos 4,5:1 sobre o fundo #1e1f29 (WCAG AA).
 */
export const Janela = styled.figure`
  margin: 16px 0;
  border-radius: 16px;
  overflow: hidden;
  background: #1e1f29;
  box-shadow: 0 10px 24px rgba(4, 4, 35, 0.22);
  border: 1px solid rgba(255, 255, 255, 0.06);

  &.console {
    background: #0d1117;
  }
`;

export const Barra = styled.div`
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 14px;
  background: #2b2d3a;
  font-family: "JetBrains Mono", Consolas, "Courier New", monospace;
  font-size: 12px;
  color: #c9ccd6;

  .bolinhas {
    display: flex;
    gap: 6px;
  }

  .bolinhas i {
    width: 11px;
    height: 11px;
    border-radius: 50%;
    background: #ff5f57;
  }

  .bolinhas i:nth-child(2) { background: #febc2e; }
  .bolinhas i:nth-child(3) { background: #28c840; }

  .aba {
    background: #1e1f29;
    padding: 3px 12px;
    border-radius: 8px 8px 0 0;
    border-bottom: 2px solid var(--orange);
  }

  .console & .aba {
    background: #0d1117;
    border-bottom-color: #3fb950;
  }
`;

export const Corpo = styled.div`
  display: flex;
  overflow-x: auto; /* linhas longas rolam de lado (no celular também), sem quebrar o código */
  font-family: "JetBrains Mono", Consolas, "Courier New", monospace;
  font-size: 14px;
  line-height: 1.7;
  padding: 14px 0;
`;

export const Linhas = styled.div`
  display: flex;
  flex-direction: column;
  text-align: right;
  padding: 0 12px 0 14px;
  color: #6b6f80;
  user-select: none; /* ao copiar o código, os números não vão junto */
  border-right: 1px solid #343646;
  min-width: 42px;
`;

export const Codigo = styled.pre`
  margin: 0;
  padding: 0 18px 0 14px;
  white-space: pre;
  color: #e6e6e6;
  font: inherit;

  .palavraChave { color: #ff9e64; font-weight: 600; }
  .texto { color: #9ece6a; }
  .comentario { color: #8b90a6; font-style: italic; }
  .numero { color: #7dcfff; }
  .tipo { color: #e0af68; }
  .metodo { color: #7aa2f7; }
  .anotacao { color: #e0d561; }

  /* lacuna da questão: destacada para o aluno ver ONDE completar */
  .lacuna {
    color: #1e1f29;
    background: #ecac0c;
    border-radius: 4px;
    box-shadow: 0 0 0 2px rgba(236, 172, 12, 0.35);
  }

  .console & {
    color: #7ee787;
  }
`;
