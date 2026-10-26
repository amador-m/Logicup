import { Link } from "react-router-dom";
import styled, { keyframes } from "styled-components";
import IconeGema from "./IconeGema";

const pulsar = keyframes`
  0%   { transform: scale(1); }
  40%  { transform: scale(1.25); }
  100% { transform: scale(1); }
`;

const Pilula = styled(Link)`
  display: inline-flex;
  align-items: center;
  gap: 6px;
  align-self: center;
  padding: 6px 12px 6px 10px;
  border-radius: 999px;
  background: rgba(241, 240, 226, 0.12);
  border: 2px solid rgba(236, 172, 12, 0.6);
  color: var(--yellow);
  font-weight: 800;
  font-size: 15px;
  white-space: nowrap;
  text-decoration: none;

  /* a cada valor novo o React recria o número (key), e a animação roda de novo */
  strong {
    display: inline-block;
    animation: ${pulsar} 0.5s ease-out;
    color: var(--cream);
  }

  &:hover {
    background: rgba(236, 172, 12, 0.18);
  }

  &:focus-visible {
    outline: 3px solid var(--yellow);
    outline-offset: 2px;
  }

  @media (prefers-reduced-motion: reduce) {
    strong {
      animation: none;
    }
  }
`;

/** Sprint 9 (F5): "💎 120" no Header, estilo Duolingo. Leva para a tela de conquistas. */
const ContadorGemas = ({ gemas }) => {
    if (gemas == null) return null;
    return (
        <Pilula to="/conquistas" title="Suas gemas: ganhe com conquistas e troque por avatares"
                aria-label={`${gemas} gemas. Ver conquistas`}>
            <IconeGema $tamanho={18} />
            <strong key={gemas}>{gemas}</strong>
        </Pilula>
    );
};

export default ContadorGemas;
