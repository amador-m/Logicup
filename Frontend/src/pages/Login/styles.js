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
  width: 333px;
  margin-bottom: 47px;
    margin-right: 100px;
`;

export const Box = styled.div`
  width: 100%;
  max-width: 1050px;
  min-height: 620px;
  background-color: var(--cream);
  border-radius: 32px;
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr 1fr;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
`;

export const LeftSide = styled.div`
  background: linear-gradient(
    160deg,
    var(--brown-dark),
    var(--orange),
    var(--yellow)
  );
  color: var(--cream);
  padding: 70px 55px;
  display: flex;
  flex-direction: column;
  justify-content: center;

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
`;

export const RightSide = styled.div`
  padding: 70px 55px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  h2 {
    font-size: 36px;
    color: var(--dark);
    margin-bottom: 8px;
  }

  p {
    color: var(--brown-dark);
    margin-bottom: 34px;
  }
`;

export const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

export const InputGroup = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px;

  label {
    font-weight: 600;
    color: var(--dark);
  }

  input {
    border: 2px solid transparent;
    background-color: var(--white);
    padding: 15px 18px;
    border-radius: 14px;
    font-size: 15px;
    outline: none;

    &:focus {
      border-color: var(--yellow);
    }
  }
`;

export const Select = styled.select`
  border: 2px solid transparent;
  background-color: var(--white);
  padding: 15px 18px;
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
  padding: 16px;
  border-radius: 16px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;

  &:hover {
    background-color: var(--dark);
  }
`;

export const LinkText = styled.p`
  text-align: center;
  font-size: 15px;

  span {
    color: var(--orange);
    font-weight: 700;
    cursor: pointer;
  }
`;