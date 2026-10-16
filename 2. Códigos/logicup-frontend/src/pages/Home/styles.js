import styled from "styled-components";

export const Section = styled.section`
  padding: 90px 120px;

  /* faixa escura com as 10 fases */
  &.trilha {
    background: linear-gradient(180deg, var(--dark), #12304a 60%, var(--green));
  }

  @media (max-width: 960px) {
    padding: 64px 20px;
  }
`;

export const SectionTitle = styled.div`
    max-width: 760px;
    margin: 0 auto 50px;
    text-align: center;

    span {
        color: var(--orange);
        font-weight: 700;
    }

    h2 {
        font-size: 38px;
        margin: 12px 0;
    }

    p {
        color: var(--brown-dark);
        line-height: 1.7;
    }

    &.light {
        color: var(--cream);

        span {
            color: var(--yellow);
        }

        p {
            color: rgba(241, 240, 226, 0.82);
        }
    }

    @media (max-width: 600px) {
        h2 {
            font-size: 28px;
        }
    }
`;

/* ---------- Como funciona: 4 passos ---------- */
export const Passos = styled.ol`
  list-style: none;
  max-width: 1150px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 22px;

  @media (max-width: 1100px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
`;

export const Passo = styled.li`
  position: relative;
  background: var(--white);
  border-radius: 26px;
  padding: 26px 24px 28px;
  text-align: center;
  box-shadow: 0 12px 30px rgba(4, 4, 35, 0.1);
  border-bottom: 6px solid var(--yellow);
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-6px);
  }

  .numero {
    position: absolute;
    top: 16px;
    left: 18px;
    width: 34px;
    height: 34px;
    border-radius: 50%;
    background: var(--dark);
    color: var(--yellow);
    font-weight: 900;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  img {
    width: 110px;
    height: auto;
    margin-bottom: 8px;
  }

  h3 {
    font-size: 21px;
    margin-bottom: 8px;
  }

  p {
    color: var(--brown-dark);
    line-height: 1.6;
    font-size: 15px;
  }
`;

/* ---------- faixa com as 10 fases (rola para o lado em telas menores) ---------- */
export const FaixaFases = styled.ol`
  list-style: none;
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 18px 14px;

  @media (max-width: 960px) {
    display: flex;
    overflow-x: auto;
    padding-bottom: 10px;
    scroll-snap-type: x mandatory;
  }
`;

export const FaseMini = styled.li`
  background: rgba(241, 240, 226, 0.08);
  border: 1px solid rgba(241, 240, 226, 0.18);
  border-radius: 22px;
  padding: 16px 12px 18px;
  text-align: center;
  color: var(--cream);
  scroll-snap-align: start;

  img {
    width: 92px;
    height: auto;
  }

  span {
    display: block;
    font-size: 12px;
    font-weight: 900;
    color: var(--yellow);
    text-transform: uppercase;
    letter-spacing: 0.06em;
    margin-top: 6px;
  }

  strong {
    display: block;
    font-size: 15px;
    margin-top: 2px;
    line-height: 1.3;
  }

  &.boss {
    border-color: var(--orange);
    background: rgba(227, 92, 51, 0.16);
  }

  @media (max-width: 960px) {
    min-width: 150px;
  }
`;

/* ---------- Gotas de saber ---------- */
export const BlogSection = styled.section`
  padding: 90px 120px;
  background-color: var(--brown-dark);

  @media (max-width: 960px) {
    padding: 64px 20px;
  }
`;

export const BlogGrid = styled.div`
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;

  @media (max-width: 960px) {
    grid-template-columns: 1fr;
  }
`;

export const BlogCard = styled.article`
  background: var(--cream);
  border-radius: 28px;
  padding: 30px;
  border: 4px solid var(--yellow);
  box-shadow: 0 18px 40px rgba(4, 4, 35, 0.28);
  transition: 0.3s;

  &:hover {
    transform: translateY(-8px);
    border-color: var(--orange);
  }

  h3 {
    color: var(--dark);
    font-size: 24px;
    line-height: 1.2;
    margin: 14px 0;
  }

  p {
    color: var(--brown-dark);
    line-height: 1.7;
    font-size: 15px;
  }
`;

export const BlogTag = styled.span`
  display: inline-block;
  background: var(--yellow);
  color: var(--dark);
  padding: 7px 14px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
`;

/* ---------- chamada final ---------- */
export const Chamada = styled.section`
  padding: 70px 24px;
  text-align: center;
  background:
    radial-gradient(circle at 20% 50%, rgba(236, 172, 12, 0.35), transparent 40%),
    radial-gradient(circle at 80% 50%, rgba(227, 92, 51, 0.3), transparent 40%),
    var(--cream);

  h2 {
    font-size: 34px;
    color: var(--dark);
    margin-bottom: 22px;
  }

  button {
    background: var(--green);
    color: var(--cream);
    border: none;
    padding: 16px 36px;
    border-radius: 18px;
    font-weight: 900;
    font-size: 16px;
    cursor: pointer;
    box-shadow: 0 6px 0 #1b5a54;
    transition: transform 0.1s, box-shadow 0.1s;

    &:active {
      transform: translateY(4px);
      box-shadow: 0 2px 0 #1b5a54;
    }
  }

  @media (max-width: 600px) {
    h2 {
      font-size: 26px;
    }
  }
`;
