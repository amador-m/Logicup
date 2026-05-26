import {
    Container,
    Menu,
    MenuItem,
    Actions,
    Logo,
    LoginButton,
    RegisterButton, Img,
    /*Img,*/
} from "./styles";
import LogoImg from "../../../assets/images/LogoLight.png";

const Header = () => {
    return (
        <Container>
            <Img><img src={LogoImg} alt="" /></Img>

            <Menu>
                <MenuItem>Início</MenuItem>
                <MenuItem>Trilha</MenuItem>
                <MenuItem>Missões</MenuItem>
                <MenuItem>Ranking</MenuItem>
                <MenuItem>Recompensas</MenuItem>
            </Menu>

            <Actions>
                <LoginButton>Entrar</LoginButton>
                <RegisterButton>Cadastrar</RegisterButton>
            </Actions>
        </Container>
    );
};

export default Header;