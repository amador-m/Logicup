import {
    Container,
    Menu,
    MenuItem,
    Actions,
    LoginButton,
    RegisterButton, Img,
    /*Img,*/
} from "./styles";
import LogoImg from "../../../assets/images/LogoLight.png";
import {Link} from "react-router-dom";
import {Logo} from "../../../pages/Login/styles.js";

const Header = () => {
    return (
        <Container>
            <Link to="/"><Img><img src={LogoImg} alt="" /></Img></Link>

            <Menu>
                <MenuItem><Link to="/">Início</Link></MenuItem>
                <MenuItem><Link to="/trilha">Trilha</Link></MenuItem>
                <MenuItem><Link to="/missao/1">Missões</Link></MenuItem>
                <MenuItem>Ranking</MenuItem>
                <MenuItem>Recompensas</MenuItem>
            </Menu>

            <Actions>
                <LoginButton as={Link} to="/login">Entrar</LoginButton>
                <RegisterButton as={Link} to="/cadastro">Cadastrar</RegisterButton>
            </Actions>
        </Container>
    );
};

export default Header;