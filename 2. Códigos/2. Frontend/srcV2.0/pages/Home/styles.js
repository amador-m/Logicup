import styled from "styled-components";

export const Section = styled.section`
  padding: 90px 120px;
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
`;

export const Cards = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
`;

export const Card = styled.div`
  background-color: var(--white);
  padding: 34px;
  border-radius: 24px;
  box-shadow: 0 12px 30px rgba(4, 4, 35, 0.1);
  border-bottom: 6px solid var(--yellow);

  h3 {
    font-size: 24px;
    margin-bottom: 12px;
  }

  p {
    color: var(--brown-dark);
    line-height: 1.6;
  }
`;

export const BlogSection = styled.section`
  padding: 90px 120px;
    background-color: var(--brown-dark);
`;

export const BlogGrid = styled.div`
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 28px;
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
    margin-bottom: 22px;
  }

  button {
    background: var(--green);
    color: var(--white);
    border: none;
    padding: 12px 22px;
    border-radius: 999px;
    font-weight: 800;
    cursor: pointer;

    &:hover {
      background: var(--orange);
    }
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

export const Line = styled.div`
  width: 70px;
  height: 4px;
  background-color: var(--cream);
`;