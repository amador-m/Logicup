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
  padding: 18px 150px;
  background: var(--dark);
  color: rgba(241, 240, 226, 0.6);
  font-size: 14px;
  border-top: 1px solid rgba(241, 240, 226, 0.12);

  @media (max-width: 960px) {
    padding: 18px 24px;
  }
`;
