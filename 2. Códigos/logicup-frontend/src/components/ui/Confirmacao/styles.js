import styled from "styled-components";

export const Dialogo = styled.dialog`
  width: min(480px, calc(100% - 32px));
  margin: auto;
  padding: 28px;
  border: 4px solid var(--orange);
  border-radius: 28px;
  background: var(--cream);
  color: var(--dark);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);

  &::backdrop {
    background: rgba(4, 4, 35, 0.6);
    backdrop-filter: blur(3px);
  }

  h2 {
    font-size: 22px;
    margin-bottom: 8px;
  }

  p {
    line-height: 1.6;
  }

  label {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 16px;
    font-size: 14px;
  }

  input {
    padding: 12px 14px;
    border-radius: 12px;
    border: 2px solid rgba(118, 73, 53, 0.3);
    font-size: 15px;
    background: var(--white);
  }

  input:focus-visible {
    outline: none;
    border-color: var(--yellow);
  }
`;

export const Botoes = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 22px;

  @media (max-width: 480px) {
    flex-direction: column-reverse;
  }
`;

const Botao = styled.button`
  padding: 12px 20px;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 800;
  cursor: pointer;

  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;

export const BotaoCancelar = styled(Botao)`
  background: transparent;
  border: 2px solid var(--brown-dark);
  color: var(--brown-dark);
`;

export const BotaoConfirmar = styled(Botao)`
  background: var(--orange);
  border: 2px solid var(--orange);
  color: var(--white);

  &:hover:not(:disabled) {
    background: var(--dark);
    border-color: var(--dark);
  }
`;

export const Erro = styled.p`
  margin-top: 12px;
  color: var(--orange);
  font-weight: 700;
  font-size: 14px;
`;
