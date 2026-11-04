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
    SuccessMessage,
} from "./styles";
import LogoImg from "../../assets/images/LogoLight.png";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/useAuth";
import { CODIGO_EMAIL_NAO_CONFIRMADO } from "../../services/api";
import ReenviarConfirmacao from "../../components/ui/ReenviarConfirmacao";

const Login = () => {
    const { login } = useAuth();

    // "componentes controlados": o React guarda o que é digitado em cada campo
    const [identificacao, setIdentificacao] = useState(""); // e-mail OU nome de usuário
    const [senha, setSenha] = useState("");
    const [erro, setErro] = useState("");
    const [enviando, setEnviando] = useState(false);
    // Sprint 10: senha certa, mas e-mail ainda não confirmado -> mostra o "reenviar link"
    const [naoConfirmado, setNaoConfirmado] = useState(false);

    const handleLogin = async (event) => {
        event.preventDefault(); // impede o navegador de recarregar a página ao enviar o form
        setErro("");
        setNaoConfirmado(false);
        setEnviando(true);

        try {
            await login(identificacao, senha);
            // deu certo: o PublicOnlyRoute percebe o usuário logado e leva para a trilha
        } catch (error) {
            // o frontend decide pelo CÓDIGO do erro, nunca pelo texto da mensagem
            if (error.codigo === CODIGO_EMAIL_NAO_CONFIRMADO) {
                setNaoConfirmado(true);
            } else {
                setErro(error.message); // ex.: "Login ou senha inválidos." (mensagem do backend)
            }
        } finally {
            setEnviando(false);
        }
    };

    return (
        // Sprint 7: sem o <Container> de página inteira; o card agora aparece dentro do ModalAuth
        <Box>
                <LeftSide>
                    <Link to="/"><Logo src={LogoImg} alt="Logo LogiCup"/></Link>
                    <h2>Continue sua trilha de aprendizado</h2>
                    <p>
                        Entre na sua conta para acessar missões, acompanhar seu progresso,
                        ganhar pontos e desbloquear recompensas
                    </p>
                </LeftSide>

                <RightSide>
                    <h2 id="titulo-login">Entrar</h2>
                    <p>Acesse sua conta para continuar jogando e aprendendo</p>

                    {naoConfirmado ? (
                        <>
                            <SuccessMessage role="alert">
                                Falta só confirmar seu e-mail! Abra o link que enviamos quando você criou a conta.
                                Não achou? Confira o spam ou peça um link novo:
                            </SuccessMessage>
                            {/* se a pessoa entrou com o e-mail, ele já vem preenchido; com o username, ela digita */}
                            <ReenviarConfirmacao emailInicial={identificacao.includes("@") ? identificacao.trim() : ""} />
                            <LinkText>
                                Já confirmou? <a href="/login" onClick={(evento) => { evento.preventDefault(); setNaoConfirmado(false); }}>
                                    Entrar
                                </a>
                            </LinkText>
                        </>
                    ) : (
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

                        {/* Sprint 10 */}
                        <LinkText>
                            <Link to="/esqueci-senha">Esqueci minha senha</Link>
                        </LinkText>

                        <LinkText>
                            Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link>
                        </LinkText>
                    </Form>
                    )}
                </RightSide>
        </Box>
    );
};

export default Login;
