import { Link } from "react-router-dom";
import { Box, LeftSide, RightSide, Logo } from "../../../pages/Login/styles";
import LogoImg from "../../../assets/images/LogoLight.png";

/*
 * Sprint 10: o "cartão" das telas de conta (confirmar e-mail, esqueci/redefinir senha), com o MESMO
 * visual do login e do cadastro: painel colorido à esquerda e o conteúdo à direita.
 * tituloId liga o <h2> ao ModalAuth (aria-labelledby), para leitores de tela anunciarem o título.
 */
const PainelAuth = ({ tituloId, titulo, subtitulo, ladoTitulo, ladoTexto, children }) => (
    <Box>
        <LeftSide>
            <Link to="/"><Logo src={LogoImg} alt="Logo LogiCup" /></Link>
            <h2>{ladoTitulo}</h2>
            <p>{ladoTexto}</p>
        </LeftSide>
        <RightSide>
            <h2 id={tituloId}>{titulo}</h2>
            {subtitulo && <p>{subtitulo}</p>}
            {children}
        </RightSide>
    </Box>
);

export default PainelAuth;
