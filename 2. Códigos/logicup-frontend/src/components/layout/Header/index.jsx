import {
    Container,
    Menu,
    MenuItem,
    Actions,
    LoginButton,
    RegisterButton,
    Img,
    Greeting,
} from "./styles";
import LogoImg from "../../../assets/images/LogoLight.png";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";

const Header = () => {
    const { usuario, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        /*
         * F3: depois de sair, vai para a Home.
         * A ORDEM importa: primeiro sai da página protegida, depois encerra a sessão.
         * Se fosse ao contrário, o PrivateRoute da página atual veria "ninguém logado"
         * e redirecionaria para /login, disputando com a navegação para a Home.
         */
        navigate("/", { replace: true });
        await logout();
    };

    // primeiro nome, para a saudação não ficar longa
    const primeiroNome = usuario?.nome.split(" ")[0];

    return (
        <Container>
            <Link to="/"><Img><img src={LogoImg} alt="Página inicial do LogicUp" /></Img></Link>

            <Menu>
                <MenuItem><Link to="/">Início</Link></MenuItem>
                <MenuItem><Link to="/trilha">Trilha</Link></MenuItem>
                <MenuItem><Link to="/missao/1">Missões</Link></MenuItem>
                <MenuItem>Ranking</MenuItem>
                <MenuItem>Recompensas</MenuItem>
            </Menu>

            <Actions>
                {usuario ? (
                    <>
                        <Greeting>Olá, {primeiroNome}</Greeting>
                        <LoginButton type="button" onClick={handleLogout}>Sair</LoginButton>
                    </>
                ) : (
                    <>
                        <LoginButton as={Link} to="/login">Entrar</LoginButton>
                        <RegisterButton as={Link} to="/cadastro">Cadastrar</RegisterButton>
                    </>
                )}
            </Actions>
        </Container>
    );
};

export default Header;
