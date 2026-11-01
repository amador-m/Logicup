import styled from "styled-components";

/* Sprint 9: visual da teoria (seções numeradas, listas com marcador, balões do Gattuccino). */

export const Cabecalho = styled.div`
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px 16px;
  margin: 8px 0 18px;

  .chip {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 14px;
    border-radius: 999px;
    background: rgba(40, 128, 120, 0.12);
    color: var(--green);
    font-weight: 700;
    font-size: 14px;
  }

  nav {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  nav a {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: grid;
    place-items: center;
    background: var(--white);
    border: 2px solid var(--orange);
    color: var(--orange);
    font-weight: 800;
    font-size: 14px;
    transition: 0.2s;
  }

  nav a:hover,
  nav a:focus-visible {
    background: var(--orange);
    color: var(--white);
  }
`;

export const Secao = styled.section`
  background: var(--white);
  border-radius: 22px;
  padding: 22px 24px 10px;
  margin-bottom: 18px;
  box-shadow: 0 6px 18px rgba(4, 4, 35, 0.06);
  scroll-margin-top: 90px; /* ao clicar no sumário, o título não fica escondido atrás do cabeçalho fixo */

  > h3 {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 0 0 14px;
    font-size: 21px;
    font-weight: 800;
    color: var(--green);
  }

  > h3 .numero {
    flex-shrink: 0;
    width: 36px;
    height: 36px;
    border-radius: 12px;
    display: grid;
    place-items: center;
    background: var(--orange);
    color: var(--white);
    font-size: 17px;
  }

  @media (max-width: 600px) {
    padding: 18px 16px 6px;

    > h3 {
      font-size: 18px;
    }
  }
`;

export const Paragrafo = styled.p`
  margin: 0 0 14px;
  line-height: 1.8;
  color: var(--brown-dark);
  max-width: 75ch; /* linhas muito longas cansam a leitura */
`;

/* código dentro do texto (ex.: System.out.println()) */
export const CodigoInline = styled.code`
  font-family: "JetBrains Mono", Consolas, "Courier New", monospace;
  font-size: 0.9em;
  background: rgba(40, 128, 120, 0.12);
  color: #1d5f59;
  padding: 1px 6px;
  border-radius: 6px;
  white-space: nowrap;
`;

export const Lista = styled.ul`
  list-style: none; /* os marcadores são desenhados abaixo (o reset global tirava as bolinhas) */
  margin: 0 0 16px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;

  li {
    position: relative;
    padding-left: 26px;
    line-height: 1.7;
    color: var(--brown-dark);
  }

  li::before {
    content: "";
    position: absolute;
    left: 6px;
    top: 0.62em;
    width: 9px;
    height: 9px;
    border-radius: 50%;
    background: var(--orange);
    box-shadow: 0 0 0 3px rgba(227, 92, 51, 0.18);
  }

  /* "termo: explicação" — o termo em destaque */
  .termo {
    font-weight: 800;
    color: var(--dark);
  }

  .termo.codigo {
    font-family: "JetBrains Mono", Consolas, "Courier New", monospace;
    font-size: 0.88em;
    font-weight: 600;
    background: #1e1f29;
    color: #ffcb8b;
    padding: 2px 8px;
    border-radius: 7px;
    white-space: pre-wrap;
  }
`;

export const Balao = styled.aside`
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin: 6px 0 18px;
  padding: 14px 18px;
  border-radius: 18px;
  background: rgba(40, 128, 120, 0.1);
  border: 2px dashed rgba(40, 128, 120, 0.45);

  img {
    width: 64px;
    flex-shrink: 0;
  }

  strong {
    display: block;
    color: var(--green);
    margin-bottom: 4px;
  }

  p {
    margin: 0;
    line-height: 1.7;
    color: var(--brown-dark);
  }

  &.alerta {
    background: rgba(227, 92, 51, 0.09);
    border-color: rgba(227, 92, 51, 0.5);
  }

  &.alerta strong {
    color: var(--orange);
  }

  @media (max-width: 600px) {
    img {
      width: 48px;
    }
  }
`;

export const RotuloExemplo = styled.span`
  display: inline-block;
  margin: 4px 0 2px;
  padding: 3px 12px;
  border-radius: 999px;
  background: var(--yellow);
  color: var(--dark);
  font-weight: 800;
  font-size: 13px;
`;

export const FimDaTeoria = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 16px;
  margin: 4px 0 8px;
  border-radius: 18px;
  background: rgba(236, 172, 12, 0.18);
  color: var(--brown-dark);
  font-weight: 700;
  text-align: left;

  img {
    width: 56px;
  }
`;
