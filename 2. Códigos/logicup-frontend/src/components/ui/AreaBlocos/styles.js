import styled from "styled-components";
import { CORES } from "../../../blocos/cores";

/* A <div> onde o Blockly desenha. A altura vem de quem usa (variável --altura); padrão 520px. */
export const Area = styled.div`
  width: 100%;
  height: var(--altura, 520px);
  min-height: 320px;
  border-radius: 0 0 18px 18px;
  overflow: hidden;

  /* textos do Blockly na fonte do site */
  .blocklyText, .blocklyToolboxCategoryLabel, .blocklyFlyoutLabelText, .blocklyFlyoutButton .blocklyText {
    font-family: "Poppins", sans-serif !important;
  }

  .blocklyFlyoutButton {
    fill: var(--orange);
  }

  .blocklyFlyoutButton .blocklyText {
    fill: var(--white);
    font-weight: 700;
  }

  /*
   * CAIXA DE FERRAMENTAS (as categorias). O Blockly desenha cada categoria como uma <div> com uma faixa
   * colorida à esquerda (estilo inline) e pinta o fundo com a cor quando ela é escolhida.
   * Aqui viramos isso em "pílulas": bolinha com a cor da categoria + nome; a escolhida fica preenchida.
   */
  .blocklyToolbox {
    padding: 10px 8px;
    border-right: 2px solid rgba(118, 73, 53, 0.15);
  }

  .blocklyToolboxCategoryContainer {
    margin: 0 0 4px;
  }

  .logicup-categoria {
    display: flex;
    align-items: center;
    gap: 10px;
    height: 40px;
    line-height: 40px;
    margin: 0;
    padding: 0 14px 0 12px !important;  /* o Blockly põe um padding inline (recuo de subcategoria) */
    border-left: none !important;       /* a faixa colorida do Blockly sai: a cor vai para a bolinha */
    border-radius: 14px;
    transition: background-color 0.15s ease, transform 0.15s ease;
  }

  .logicup-categoria::before {
    content: "";
    flex: none;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    background: var(--cor-categoria);
    box-shadow: inset 0 -2px 0 rgba(0, 0, 0, 0.18);
  }

  ${Object.entries(CORES).map(([nome, cor]) => `.logicup-categoria-${nome} { --cor-categoria: ${cor}; }`).join("\n  ")}

  /* o ícone de pasta do Blockly não é usado (não temos subcategorias) */
  .logicup-categoria .blocklyToolboxCategoryIcon {
    display: none !important;  /* o Blockly põe display inline no ícone */
  }

  .logicup-categoria:not(.blocklyToolboxSelected):hover {
    background-color: rgba(118, 73, 53, 0.1);
  }

  @media (prefers-reduced-motion: no-preference) {
    .blocklyToolbox[layout="v"] .logicup-categoria:not(.blocklyToolboxSelected):hover {
      transform: translateX(2px);
    }
  }

  .blocklyToolboxCategoryLabel {
    padding: 0;
    font-size: 14px;
    font-weight: 600;
  }

  /* categoria escolhida: o Blockly pinta o fundo com a cor dela; texto e bolinha ficam brancos */
  .blocklyToolboxSelected {
    box-shadow: 0 3px 0 rgba(0, 0, 0, 0.18);
  }

  .blocklyToolboxSelected::before {
    background: var(--white);
    box-shadow: none;
  }

  .blocklyToolboxSelected .blocklyToolboxCategoryLabel {
    color: var(--white) !important;
    font-weight: 700;
  }

  /* celular: a caixa fica deitada em cima (layout="h"), com as pílulas lado a lado e mais compactas */
  .blocklyToolbox[layout="h"] {
    padding: 6px 6px 2px;
    border-right: none;
    border-bottom: 2px solid rgba(118, 73, 53, 0.15);
  }

  .blocklyToolbox[layout="h"] .blocklyToolboxCategoryContainer {
    margin: 0 4px 4px 0;
  }

  .blocklyToolbox[layout="h"] .logicup-categoria {
    gap: 7px;
    height: 34px;
    line-height: 34px;
    padding: 0 11px 0 9px !important;
  }

  .blocklyToolbox[layout="h"] .logicup-categoria::before {
    width: 11px;
    height: 11px;
  }

  .blocklyToolbox[layout="h"] .blocklyToolboxCategoryLabel {
    font-size: 13px;
  }
`;

export const Carregando = styled.div`
  height: var(--altura, 520px);
  display: grid;
  place-items: center;
  color: var(--brown-dark);
  font-weight: 600;
  background: #fbf9ef;
  border-radius: 0 0 18px 18px;
`;
