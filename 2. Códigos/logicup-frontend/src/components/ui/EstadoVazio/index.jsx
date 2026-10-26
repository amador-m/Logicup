import styled, { keyframes } from "styled-components";
import GattuccinoDormindo from "../../../assets/images/gattuccino-dormindo.png";

/* os "z" sobem e somem, como num desenho animado */
const sonhar = keyframes`
  0%   { opacity: 0; transform: translate(0, 6px) scale(0.6); }
  30%  { opacity: 1; }
  100% { opacity: 0; transform: translate(14px, -26px) scale(1.1); }
`;

const Caixa = styled.div`
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 18px 22px;
  border-radius: 20px;
  background: rgba(118, 73, 53, 0.08);
  color: var(--dark);

  .gato {
    position: relative;
    flex: 0 0 auto;
  }

  img {
    width: ${({ $tamanho }) => $tamanho}px;
    height: auto;
    display: block;
  }

  .z {
    position: absolute;
    top: 6%;
    right: 4%;
    font-weight: 900;
    color: var(--brown-dark);
    animation: ${sonhar} 2.4s ease-in infinite;
  }

  .z:nth-of-type(2) { animation-delay: 0.8s; font-size: 1.2em; }
  .z:nth-of-type(3) { animation-delay: 1.6s; font-size: 1.4em; }

  .titulo {
    display: block;
    font-size: 17px;
    margin-bottom: 4px;
  }

  p {
    margin: 0;
    line-height: 1.5;
    color: var(--brown-dark);
  }

  /* fundo escuro (ex.: cartão "Sua posição" do ranking) */
  &.sobre-escuro {
    background: rgba(241, 240, 226, 0.1);
    color: var(--cream);

    p, .z {
      color: rgba(241, 240, 226, 0.8);
    }
  }

  @media (max-width: 520px) {
    flex-direction: column;
    text-align: center;
  }

  @media (prefers-reduced-motion: reduce) {
    .z {
      display: none;
    }
  }
`;

/**
 * Sprint 9 (E2): estado vazio com o Gattuccino dormindo ("nada acontecendo aqui ainda").
 * Use só onde NÃO há uma ação esperando: onde existe convite (ex.: digitar o código da turma),
 * o Gattuccino acordado continua.
 */
const EstadoVazio = ({ titulo, children, tamanho = 96, className }) => (
    <Caixa $tamanho={tamanho} className={className}>
        <div className="gato" aria-hidden="true">
            <img src={GattuccinoDormindo} alt="" />
            <span className="z">z</span><span className="z">z</span><span className="z">Z</span>
        </div>
        <div>
            {titulo && <strong className="titulo">{titulo}</strong>}
            {children && <p>{children}</p>}
        </div>
    </Caixa>
);

export default EstadoVazio;
