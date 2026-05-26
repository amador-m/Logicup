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

export const PathSection = styled.section`
  background-color: var(--brown-dark);
  padding: 80px 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;

  h2 {
    color: var(--cream);
    margin-right: 35px;
  }
`;

export const MissionNode = styled.div`
  width: 72px;
  height: 72px;
  background-color: var(--yellow);
  color: var(--dark);
  border: 5px solid var(--cream);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;

  &.orange {
    background-color: var(--orange);
    color: var(--white);
  }

  &.locked {
    background-color: var(--brown-light);
    color: var(--cream);
  }
`;

export const Line = styled.div`
  width: 70px;
  height: 4px;
  background-color: var(--cream);
`;