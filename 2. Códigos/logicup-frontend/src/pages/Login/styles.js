import styled from "styled-components";

export const Container = styled.main`
  min-height: 100vh;
  background: linear-gradient(135deg, var(--dark), var(--brown-dark));
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
`;

export const Logo = styled.img`
  width: 230px;
  margin-bottom: 28px;
`;

export const Box = styled.div`
  width: 100%;
  max-width: 1050px;
  min-height: 560px;
  background-color: var(--cream);
  border-radius: 32px;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr 1fr;
  /* Sprint 10: o cartão nunca passa da altura da tela (com 16 px de folga em cima e embaixo) */
  max-height: calc(100dvh - 32px);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);

  /* Sprint 7 (modal): em telas estreitas, só o formulário */
  @media (max-width: 800px) {
    /* minmax(0, 1fr): deixa a coluna encolher abaixo da largura "natural" dos inputs */
    grid-template-columns: minmax(0, 1fr);
  }
`;

export const LeftSide = styled.div`
  background: linear-gradient(
    160deg,
    var(--brown-dark),
    var(--orange),
    var(--yellow)
  );
  color: var(--cream);
  padding: 48px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;   /* logo + texto no MEIO da parte visível (antes, do cartão inteiro, que era alto) */

  h1 {
    font-size: 42px;
    margin-bottom: 60px;

    span {
      color: var(--yellow);
    }
  }

  h2 {
    font-size: 38px;
    line-height: 1.2;
    margin-bottom: 24px;
  }

  p {
    line-height: 1.7;
    font-size: 17px;
  }

  @media (max-width: 800px) {
    display: none;
  }
`;

export const RightSide = styled.div`
  padding: 32px 50px 24px;

  @media (max-width: 600px) {
    padding: 56px 22px 28px;
  }

  display: flex;
  flex-direction: column;
  /* sem "justify-content: center": quando o conteúdo é maior que a coluna, ele cortaria o TOPO
     (e a rolagem não alcança). Quem centraliza são as margens automáticas no fim deste bloco. */

  /* Sprint 10: só esta coluna rola quando o formulário é maior que a tela (o cadastro, em notebooks) */
  max-height: calc(100dvh - 32px);
  overflow-y: auto;
  overscroll-behavior: contain;     /* rolar até o fim não "passa" a rolagem para a página de trás */

  /* barra de rolagem fina, nas cores do LogiCup, sem o trilho branco */
  scrollbar-width: thin;                                  /* Firefox e Chrome/Edge 121+ */
  scrollbar-color: rgba(118, 73, 53, 0.45) transparent;
  &::-webkit-scrollbar { width: 8px; }                    /* Safari e navegadores mais antigos */
  &::-webkit-scrollbar-track { background: transparent; margin: 24px 0; }
  &::-webkit-scrollbar-thumb { background: rgba(118, 73, 53, 0.45); border-radius: 8px; }

  /* com justify-content: center, um conteúdo mais alto que a coluna ficaria cortado EM CIMA;
     o "margin-block: auto" nos filhos centraliza quando cabe e começa do topo quando não cabe */
  & > :first-child { margin-top: auto; }
  & > :last-child { margin-bottom: auto; }

  h2 {
    font-size: 36px;
    color: var(--dark);
    margin-bottom: 4px;
  }

  p {
    color: var(--brown-dark);
    margin-bottom: 20px;
  }
`;

export const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

export const InputGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 6px;

  label {
    font-weight: 600;
    color: var(--dark);
  }

  input {
    border: 2px solid transparent;
    background-color: var(--white);
    padding: 12px 16px;
    border-radius: 14px;
    font-size: 15px;
    outline: none;

    &:focus {
      border-color: var(--yellow);
    }
  }

  /* Sprint 7: texto de ajuda embaixo do campo (ex.: regra do nome de usuário) */
  small {
    color: var(--brown-dark);
    font-size: 13px;
    line-height: 1.4;
  }
`;

export const Select = styled.select`
  border: 2px solid transparent;
  background-color: var(--white);
  padding: 12px 16px;
  border-radius: 14px;
  font-size: 15px;
  outline: none;

  &:focus {
    border-color: var(--yellow);
  }
`;

export const Button = styled.button`
  background-color: var(--green);
  color: var(--white);
  border: none;
  padding: 14px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;

  &:hover {
    background-color: var(--dark);
  }

  /* enquanto o formulário é enviado */
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

/* mensagem de erro geral do formulário (ex.: "E-mail ou senha inválidos.") */
export const ErrorMessage = styled.p`
  color: var(--orange-texto);
  font-weight: 600;
  font-size: 14px;
`;

/* mensagem de erro de um campo específico, embaixo do input */
export const FieldError = styled.span`
  color: var(--orange-texto);
  font-size: 13px;
  font-weight: 500;
`;

export const LinkText = styled.p`
  text-align: center;
  font-size: 15px;

  /*
   * Sprint 9: o link ("Cadastre-se" no login, "Entrar" no cadastro) é um <Link>, que vira <a> no HTML.
   * A regra antiga estilizava um "span" que não existe nessa tela, e o reset global
   * (a { color: inherit; text-decoration: none }) deixava o link igual ao texto em volta.
   * Cor do texto: marrom escuro sobre o creme = contraste 6,6:1 (WCAG AA pede 4,5:1).
   * O laranja como cor do texto teria só 3,1:1; por isso ele entra no SUBLINHADO, que chama a atenção
   * sem prejudicar a leitura.
   */
  a {
    color: var(--brown-dark);
    font-weight: 800;
    text-decoration: underline;
    text-decoration-color: var(--orange);
    text-decoration-thickness: 2px;
    text-underline-offset: 3px;
  }

  a:hover,
  a:focus-visible {
    text-decoration-thickness: 3px; /* reforça o sublinhado no hover/foco (teclado) sem trocar a cor */
  }
`;
/* Sprint 10: aviso de sucesso/informação (ex.: "enviamos um link para o seu e-mail") */
export const SuccessMessage = styled.p`
  background: rgba(40, 128, 120, 0.12);
  border-left: 4px solid var(--green);
  color: var(--dark);
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
`;

/* Sprint 10: ação secundária com cara de link (ex.: "Esqueci minha senha", "Reenviar link") */
export const LinkButton = styled.button`
  align-self: flex-start;
  background: none;
  border: none;
  padding: 0;
  font: inherit;
  font-size: 14px;
  font-weight: 700;
  color: var(--brown-dark);
  text-decoration: underline;
  text-decoration-color: var(--orange);
  text-decoration-thickness: 2px;
  text-underline-offset: 3px;
  cursor: pointer;

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

/* Sprint 10: botão "Entrar com Google" (um link: o login acontece no backend) e o "ou" que separa do formulário */
/*
 * Botão "Entrar com Google" no tema CLARO das regras de marca do Google:
 * fundo #FFFFFF, borda de 1px #747775, texto #1F1F1F, G colorido oficial à esquerda.
 * As cores do botão NÃO mudam no hover (as regras não permitem trocar a borda/fundo por outra cor);
 * o retorno visual é um fundo levemente acinzentado e uma sombra. O contorno verde do teclado (focus-visible)
 * fica FORA do botão (outline + offset), por acessibilidade.
 */
export const BotaoGoogle = styled.a`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 16px;
  border: 1px solid #747775;
  background: #ffffff;
  color: #1f1f1f;
  font-weight: 600;
  font-size: 15px;
  text-decoration: none;
  transition: background-color 0.15s ease, box-shadow 0.15s ease;

  svg {
    flex-shrink: 0;
  }

  &:hover {
    background: #f8f9fa;
    box-shadow: 0 1px 3px rgba(60, 64, 67, 0.3);
  }

  &:focus-visible {
    outline: 3px solid var(--green);
    outline-offset: 2px;
  }

  @media (prefers-reduced-motion: reduce) {
    transition: none;
  }
`;

export const Separador = styled.p`
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--brown-dark);
  font-size: 13px;

  &::before,
  &::after {
    content: "";
    flex: 1;
    height: 1px;
    background: rgba(118, 73, 53, 0.3);
  }
`;
