import MascoteImg from "../../../assets/images/MascoteRecortado.png";
import { MascoteBox } from "../styles";

/*
 * Mascote que REAGE ao que acontece na missão. Só CSS (sem arte nova):
 *   "neutro" -> entra e fica flutuando     "acerto" -> pula comemorando     "erro" -> balança
 * A animação está nas classes do MascoteBox (styles.js). Quem usa este componente deve trocar
 * a "key" quando o humor mudar, para o React recriar o elemento e a animação tocar de novo.
 * Com "reduzir movimento" ativado no sistema, as animações são desligadas (ver styles.js).
 *
 * A fala fica num balão com aria-live: leitores de tela anunciam a reação junto com o feedback.
 */
const MascoteReacao = ({ humor = "neutro", fala, tamanho = 96 }) => (
    <MascoteBox className={humor} style={{ "--tamanho": `${tamanho}px` }}>
        <img src={MascoteImg} alt="" aria-hidden="true" />
        {fala && <p aria-live="polite">{fala}</p>}
    </MascoteBox>
);

export default MascoteReacao;
