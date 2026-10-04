import { useState } from "react";
import {
    Container,
    Box,
    LeftSide,
    RightSide,
    Form,
    InputGroup,
    Button,
    LinkText,
    Logo,
    ErrorMessage,
} from "./styles";
import LogoImg from "../../assets/images/LogoLight.png";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/useAuth";

const Login = () => {
    const { login } = useAuth();

    // "componentes controlados": o React guarda o que é digitado em cada campo
    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");
    const [erro, setErro] = useState("");
    const [enviando, setEnviando] = useState(false);

    const handleLogin = async (event) => {
        event.preventDefault(); // impede o navegador de recarregar a página ao enviar o form
        setErro("");
        setEnviando(true);

        try {
            await login(email, senha);
            // deu certo: o PublicOnlyRoute percebe o usuário logado e leva para a trilha
        } catch (error) {
            setErro(error.message); // ex.: "E-mail ou senha inválidos." (mensagem do backend)
        } finally {
            setEnviando(false);
        }
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
                            <label htmlFor="login-email">E-mail</label>
                            <input
                                id="login-email"
                                type="email"
                                placeholder="Digite seu e-mail"
                                autoComplete="email"
                                required
                                value={email}
                                onChange={(event) => setEmail(event.target.value)}
                            />
                        </InputGroup>

                        <InputGroup>
                            <label htmlFor="login-senha">Senha</label>
                            <input
                                id="login-senha"
                                type="password"
                                placeholder="Digite sua senha"
                                autoComplete="current-password"
                                required
                                value={senha}
                                onChange={(event) => setSenha(event.target.value)}
                            />
                        </InputGroup>

                        {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}

                        <Button type="submit" disabled={enviando}>
                            {enviando ? "Entrando..." : "Entrar"}
                        </Button>

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
