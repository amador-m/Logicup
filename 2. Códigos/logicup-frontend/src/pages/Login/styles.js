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
  margin-bottom: 36px;
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
  padding: 60px 50px;
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

  @media (max-width: 800px) {
    display: none;
  }
`;

export const RightSide = styled.div`
  padding: 60px 50px;

  @media (max-width: 600px) {
    padding: 56px 22px 28px;
  }

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

  /* enquanto o formulário é enviado */
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

/* mensagem de erro geral do formulário (ex.: "E-mail ou senha inválidos.") */
export const ErrorMessage = styled.p`
  color: var(--orange);
  font-weight: 600;
  font-size: 14px;
`;

/* mensagem de erro de um campo específico, embaixo do input */
export const FieldError = styled.span`
  color: var(--orange);
  font-size: 13px;
  font-weight: 500;
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