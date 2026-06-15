import {
    Container,
    Box,
    LeftSide,
    RightSide,
    Form,
    InputGroup,
    Button,
    LinkText,
    Logo
} from "./styles";
import LogoImg from "../../assets/images/LogoLight.png";
import { Link, useNavigate } from "react-router-dom";

const Login = () => {
    const navigate = useNavigate();

    const handleLogin = (event) => {
        event.preventDefault();
        navigate("/trilha");
    };

    return (
        <Container>
            <Box>
                <LeftSide>
                    <Link to="/"><Logo src={LogoImg} alt="Logo Logicup"/></Link>
                    <h2>Continue sua trilha de aprendizado</h2>
                    <p>
                        Entre na sua conta para acessar missões, acompanhar seu progresso,
                        ganhar pontos e desbloquear recompensas
                    </p>
                </LeftSide>

                <RightSide>
                    <h2>Entrar</h2>
                    <p>Acesse sua conta para continuar jogando e aprendendo</p>

                    <Form onSubmit={handleLogin}>
                        <InputGroup>
                            <label>E-mail</label>
                            <input type="email" placeholder="Digite seu e-mail" />
                        </InputGroup>

                        <InputGroup>
                            <label>Senha</label>
                            <input type="password" placeholder="Digite sua senha" />
                        </InputGroup>

                        <Button>Entrar</Button>

                        <LinkText>
                            Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link>
                        </LinkText>
                    </Form>
                </RightSide>
            </Box>
        </Container>
    );
};

export default Login;