import styled from "styled-components";

export const Container = styled.header`
  width: 100%;
  height: 82px;
  padding: 0 120px;
  background-color: var(--brown-dark);
  color: var(--cream);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative; /* referência para o painel do menu no celular */
  z-index: 50;

  /* Sprint 7: telas médias — menos margem para tudo caber em uma linha */
  @media (max-width: 1100px) {
    padding: 0 40px;
  }

  @media (max-width: 900px) {
    padding: 0 20px;
    height: 72px;
  }
`;

export const Img = styled.div`
    img{
        width: 170px;
        display: block;
    }

    @media (max-width: 900px) {
        img {
            width: 140px;
        }
    }
`
export const Logo = styled.h1`
  font-size: 30px;
  font-weight: 800;

  span {
    color: var(--yellow);
  }
`;

export const Menu = styled.ul`
  display: flex;
  gap: 30px;

  @media (max-width: 1100px) {
    gap: 20px;
  }

  /* celular: links um embaixo do outro, dentro do painel */
  @media (max-width: 900px) {
    flex-direction: column;
    gap: 4px;
  }
`;

export const MenuItem = styled.li`
  font-size: 15px;
  font-weight: 500;
  transition: 0.3s;
  white-space: nowrap; /* "Recompensas em breve" não quebra mais em duas linhas */

  a {
    display: inline-block;
    padding: 6px 2px;
    border-bottom: 3px solid transparent;
  }

  a:hover {
    color: var(--yellow);
  }

  /* página atual (classe colocada pelo NavLink) */
  a.active {
    color: var(--yellow);
    font-weight: 700;
    border-bottom-color: var(--yellow);
  }

  /* item ainda não disponível: sem clique, com selo "em breve" */
  &.em-breve {
    color: rgba(241, 240, 226, 0.55);
    cursor: default;
    padding: 6px 2px 9px; /* mesma altura dos links (que têm padding + borda) */
  }

  small {
    margin-left: 4px;
    font-size: 10px;
    font-weight: 800;
    text-transform: uppercase;
    background: rgba(241, 240, 226, 0.15);
    color: var(--cream);
    padding: 2px 6px;
    border-radius: 999px;
  }

  /* celular: área de toque maior (mínimo ~44px de altura) */
  @media (max-width: 900px) {
    font-size: 17px;

    a {
      display: block;
      padding: 12px 4px;
    }

    &.em-breve {
      padding: 12px 4px;
    }
  }
`;

export const Actions = styled.div`
  display: flex;
  gap: 12px;

  @media (max-width: 900px) {
    flex-wrap: wrap;
    align-items: center;
    padding-top: 16px;
    margin-top: 8px;
    border-top: 1px solid rgba(241, 240, 226, 0.18);
  }
`;

/*
 * Sprint 7: agrupa menu + botões da conta.
 * Desktop: "display: contents" faz o navegador ignorar esta caixa no layout,
 * como se Menu e Actions fossem filhos diretos do Container (o visual de antes não muda).
 * Celular: vira um painel logo abaixo do Header, escondido até o botão ☰ ser tocado.
 */
export const Navegacao = styled.div`
  display: contents;

  @media (max-width: 900px) {
    display: none;
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    flex-direction: column;
    padding: 12px 20px 24px;
    background-color: var(--brown-dark);
    border-top: 1px solid rgba(241, 240, 226, 0.18);
    box-shadow: 0 18px 30px rgba(0, 0, 0, 0.35);

    &.aberto {
      display: flex;
    }
  }
`;

/* Sprint 7: botão do menu hambúrguer — três barras que viram um "X" quando o menu abre */
export const BotaoMenu = styled.button`
  display: none;

  @media (max-width: 900px) {
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 5px;
    width: 46px;
    height: 46px;
    padding: 0 11px;
    border: 2px solid rgba(241, 240, 226, 0.6);
    border-radius: 14px;
    background: transparent;
    cursor: pointer;

    span {
      display: block;
      height: 3px;
      border-radius: 3px;
      background: var(--cream);
      transition: transform 0.25s, opacity 0.25s;
    }

    /* aberto: a barra de cima e a de baixo giram e formam o X; a do meio some */
    &.aberto span:nth-child(1) {
      transform: translateY(8px) rotate(45deg);
    }

    &.aberto span:nth-child(2) {
      opacity: 0;
    }

    &.aberto span:nth-child(3) {
      transform: translateY(-8px) rotate(-45deg);
    }

    &:focus-visible {
      outline: 3px solid var(--yellow);
      outline-offset: 2px;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    span {
      transition: none;
    }
  }
`;

export const LoginButton = styled.button`
  background-color: transparent;
  color: var(--cream);
  border: 2px solid var(--cream);
  padding: 10px 22px;
  border-radius: 25px;
  font-weight: 600;
  cursor: pointer;

    &:hover {
        background-color: var(--green);
        color: var(--white);
    }
`;

/* saudação ao usuário logado */
export const Greeting = styled.span`
  color: var(--cream);
  font-weight: 600;
  align-self: center;
  text-decoration: none;
  border-bottom: 2px solid transparent;
  padding: 4px 2px;

  /* Sprint 7: virou link para /perfil */
  &:hover,
  &.active {
    color: var(--yellow);
    border-bottom-color: var(--yellow);
  }
`;

export const RegisterButton = styled.button`
  background-color: var(--yellow);
  color: var(--brown-dark);
  border: none;
  padding: 12px 24px;
  border-radius: 25px;
  font-weight: 700;
  cursor: pointer;

  &:hover {
    background-color: var(--orange);
    color: var(--white);
  }
`;