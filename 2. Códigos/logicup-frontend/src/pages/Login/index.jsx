import { useState } from "react";
import {
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
    const [identificacao, setIdentificacao] = useState(""); // e-mail OU nome de usuário
    const [senha, setSenha] = useState("");
    const [erro, setErro] = useState("");
    const [enviando, setEnviando] = useState(false);

    const handleLogin = async (event) => {
        event.preventDefault(); // impede o navegador de recarregar a página ao enviar o form
        setErro("");
        setEnviando(true);

        try {
            await login(identificacao, senha);
            // deu certo: o PublicOnlyRoute percebe o usuário logado e leva para a trilha
        } catch (error) {
            setErro(error.message); // ex.: "Login ou senha inválidos." (mensagem do backend)
        } finally {
            setEnviando(false);
        }
    };

    return (
        // Sprint 7: sem o <Container> de página inteira; o card agora aparece dentro do ModalAuth
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
                    <h2 id="titulo-login">Entrar</h2>
                    <p>Acesse sua conta para continuar jogando e aprendendo</p>

                    <Form onSubmit={handleLogin}>
                        <InputGroup>
                            <label htmlFor="login-identificacao">E-mail ou nome de usuário</label>
                            {/* type="text" (e não "email"): o navegador recusaria um username sem "@" */}
                            <input
                                id="login-identificacao"
                                type="text"
                                placeholder="Digite seu e-mail ou nome de usuário"
                                autoComplete="username"
                                autoCapitalize="none"
                                spellCheck={false}
                                required
                                value={identificacao}
                                onChange={(event) => setIdentificacao(event.target.value)}
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
    );
};

export default Login;
