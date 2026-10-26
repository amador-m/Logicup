import styled from "styled-components";
import gema from "../../../assets/icones/gem.png";

/*
 * Sprint 9: o ícone da gema (arte da equipe, pasta "Ícones"). O PNG é preto; com mask-image ele vira
 * uma "forma" pintada pela cor do texto (currentColor). Assim o MESMO arquivo serve no Header escuro
 * (gema creme/amarela) e nas telas claras (gema verde), sem precisar de uma imagem para cada cor.
 */
const IconeGema = styled.span.attrs({ "aria-hidden": "true" })`
  display: inline-block;
  flex: 0 0 auto;
  width: ${({ $tamanho = 18 }) => $tamanho}px;
  height: ${({ $tamanho = 18 }) => $tamanho}px;
  background-color: currentColor;
  -webkit-mask: url("${gema}") center / contain no-repeat;
  mask: url("${gema}") center / contain no-repeat;
`;

export default IconeGema;
