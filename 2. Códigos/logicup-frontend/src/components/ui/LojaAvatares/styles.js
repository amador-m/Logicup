import styled, { keyframes } from "styled-components";

const aparecer = keyframes`
  from { opacity: 0; transform: translateY(24px) scale(0.97); }
  to   { opacity: 1; transform: none; }
`;

const desbloquear = keyframes`
  0%   { transform: scale(1); }
  35%  { transform: scale(1.12) rotate(-4deg); }
  70%  { transform: scale(0.97) rotate(2deg); }
  100% { transform: scale(1); }
`;

export const Dialogo = styled.dialog`
  width: min(780px, calc(100% - 24px));
  max-height: calc(100vh - 24px);
  margin: auto;
  padding: 0;
  border: 5px solid var(--yellow);
  border-radius: 28px;
  background: var(--cream);
  color: var(--dark);
  box-shadow: 0 25px 70px rgba(0, 0, 0, 0.35);
  animation: ${aparecer} 0.25s ease-out;

  &[open] {
    display: flex;
    flex-direction: column;
  }

  &::backdrop {
    background: rgba(4, 4, 35, 0.65);
    backdrop-filter: blur(4px);
  }

  @media (prefers-reduced-motion: reduce) {
    animation: none;
  }
`;

export const Topo = styled.div`
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 20px 24px 14px;
  border-bottom: 2px solid rgba(118, 73, 53, 0.15);

  h2 {
    font-size: 22px;
    margin: 0;
    outline: none;
  }

  p {
    font-size: 14px;
    color: var(--brown-dark);
    margin: 2px 0 0;
  }

  > div {
    flex: 1;
  }
`;

export const Saldo = styled.span`
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  background: var(--dark);
  color: var(--yellow);
  font-weight: 900;
  font-size: 16px;
  white-space: nowrap;

  strong {
    color: var(--cream);
  }
`;

export const Fechar = styled.button`
  width: 40px;
  height: 40px;
  flex: 0 0 auto;
  border: none;
  border-radius: 12px;
  background: rgba(118, 73, 53, 0.12);
  color: var(--brown-dark);
  font-size: 20px;
  font-weight: 900;
  cursor: pointer;

  &:hover {
    background: rgba(118, 73, 53, 0.25);
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }
`;

export const Corpo = styled.div`
  overflow-y: auto;
  padding: 18px 24px 24px;
`;

export const Mensagem = styled.p`
  margin: 0 0 14px;
  padding: 10px 14px;
  border-radius: 12px;
  font-weight: 700;
  font-size: 14px;
  background: ${({ $tipo }) => ($tipo === "erro" ? "rgba(227, 92, 51, 0.15)" : "rgba(40, 128, 120, 0.15)")};
  color: ${({ $tipo }) => ($tipo === "erro" ? "#a83a1b" : "var(--green)")};
`;

export const Grade = styled.ul`
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  list-style: none;
  padding: 0;
  margin: 0;

  @media (max-width: 680px) {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
`;

export const Item = styled.li`
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 10px 12px;
  border-radius: 20px;
  background: var(--white);
  border: 3px solid ${({ $emUso }) => ($emUso ? "var(--green)" : "rgba(118, 73, 53, 0.15)")};
  text-align: center;

  img {
    transition: filter 0.2s;
  }

  /* ainda não é seu: imagem apagada, para dar vontade de desbloquear */
  &.bloqueado img {
    filter: grayscale(0.85) brightness(0.9);
  }

  &.novo {
    animation: ${desbloquear} 0.6s ease-out;
  }

  strong {
    font-size: 15px;
    line-height: 1.2;
  }

  @media (prefers-reduced-motion: reduce) {
    &.novo {
      animation: none;
    }
  }
`;

export const Selo = styled.span`
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 900;
  text-transform: uppercase;
  color: var(--white);
  background: ${({ $cor }) => $cor};
`;

export const Preco = styled.span`
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-weight: 900;
  color: var(--green);
`;

export const Acao = styled.button`
  width: 100%;
  padding: 9px 8px;
  border-radius: 12px;
  border: none;
  font-weight: 800;
  font-size: 14px;
  cursor: pointer;
  background: ${({ $variante }) => ({ comprar: "var(--yellow)", usar: "var(--green)", confirmar: "var(--orange)" }[$variante] ?? "rgba(118, 73, 53, 0.12)")};
  color: ${({ $variante }) => ($variante === "comprar" ? "var(--dark)" : $variante ? "var(--white)" : "var(--brown-dark)")};

  &:hover:not(:disabled) {
    filter: brightness(1.08);
  }

  &:disabled {
    cursor: not-allowed;
    opacity: 0.65;
  }

  &:focus-visible {
    outline: 3px solid var(--dark);
    outline-offset: 2px;
  }
`;

export const Dica = styled.small`
  font-size: 12px;
  color: var(--brown-dark);
  line-height: 1.3;
`;
