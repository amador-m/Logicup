import styled from "styled-components";

export const Container = styled.footer`
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr 1.2fr;
  gap: 32px;
  padding: 70px 150px 50px;
  background: var(--dark);
  color: var(--cream);

  @media (max-width: 960px) {
    grid-template-columns: 1fr 1fr;
    padding: 50px 24px;
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
`;

export const Item = styled.div`
  img {
    width: 180px;
  }

  p {
    margin-top: 12px;
    line-height: 1.6;
    color: rgba(241, 240, 226, 0.75);
  }

  h3 {
    margin-bottom: 10px;
    color: var(--yellow);
    font-size: 16px;
  }

  li {
    padding: 8px 0;
  }

  a:hover {
    color: var(--yellow);
  }
`;

export const Copyright = styled.div`
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 10px 24px;
  padding: 18px 150px;
  background: var(--dark);
  color: rgba(241, 240, 226, 0.6);
  font-size: 14px;
  border-top: 1px solid rgba(241, 240, 226, 0.12);

  /* Sprint 9: link do crédito dos ícones */
  p a {
    color: inherit;
    text-decoration: underline;
  }

  p a:hover {
    color: var(--yellow);
  }

  @media (max-width: 960px) {
    padding: 18px 24px;
  }
`;

/* Sprint 8: links para Termos, Privacidade e Cookies, na faixa do copyright */
export const LinksLegais = styled.nav`
  display: flex;
  flex-wrap: wrap;
  gap: 8px 20px;

  a {
    text-decoration: underline;
    text-underline-offset: 3px;
  }

  a:hover {
    color: var(--yellow);
  }

  a:focus-visible {
    outline: 2px solid var(--yellow);
    outline-offset: 2px;
    border-radius: 4px;
  }
`;
