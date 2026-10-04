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
    Select,
    Logo,
    ErrorMessage,
    FieldError,
} from "../Login/styles";
import LogoImg from "../../assets/images/LogoLight.png";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/useAuth";

const DADOS_INICIAIS = { nome: "", email: "", senha: "", perfil: "ALUNO" };

const Register = () => {
    const { cadastrar } = useAuth();

    const [dados, setDados] = useState(DADOS_INICIAIS);
    const [erro, setErro] = useState("");              // erro geral (ex.: e-mail já cadastrado)
    const [errosCampos, setErrosCampos] = useState({}); // erros por campo vindos do backend
    const [enviando, setEnviando] = useState(false);

    // um único handler para todos os campos: usa o "name" do input para saber qual atualizar
    const handleChange = (event) => {
        const { name, value } = event.target;
        setDados((anteriores) => ({ ...anteriores, [name]: value }));
    };

    const handleRegister = async (event) => {
        event.preventDefault();
        setErro("");
        setErrosCampos({});
        setEnviando(true);

        try {
            await cadastrar(dados); // C1: já entra logado; o PublicOnlyRoute leva para a trilha
        } catch (error) {
            setErrosCampos(error.campos ?? {});
            setErro(error.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <Container>
            <Box>
                <LeftSide>
                    <Link to="/"><Logo src={LogoImg} alt="Logo Logicup"/></Link>
                    <h2>Comece sua jornada em Java</h2>
                    <p>
                        Crie sua conta para acessar a trilha, concluir missões e evoluir
                        no aprendizado de programação
                    </p>
                </LeftSide>

                <RightSide>
                    <h2>Criar conta</h2>
                    <p>Preencha seus dados para começar a aprender no Logicup</p>

                    <Form onSubmit={handleRegister}>
                        <InputGroup>
                            <label htmlFor="cadastro-nome">Nome</label>
                            <input
                                id="cadastro-nome"
                                name="nome"
                                type="text"
                                placeholder="Digite seu nome"
                                autoComplete="name"
                                required
                                value={dados.nome}
                                onChange={handleChange}
                            />
                            {errosCampos.nome && <FieldError>{errosCampos.nome}</FieldError>}
                        </InputGroup>

                        <InputGroup>
                            <label htmlFor="cadastro-email">E-mail</label>
                            <input
                                id="cadastro-email"
                                name="email"
                                type="email"
                                placeholder="Digite seu e-mail"
                                autoComplete="email"
                                required
                                value={dados.email}
                                onChange={handleChange}
                            />
                            {errosCampos.email && <FieldError>{errosCampos.email}</FieldError>}
                        </InputGroup>

                        <InputGroup>
                            <label htmlFor="cadastro-senha">Senha</label>
                            <input
                                id="cadastro-senha"
                                name="senha"
                                type="password"
                                placeholder="Crie uma senha (mínimo 8 caracteres)"
                                autoComplete="new-password"
                                required
                                minLength={8}
                                maxLength={72}
                                value={dados.senha}
                                onChange={handleChange}
                            />
                            {errosCampos.senha && <FieldError>{errosCampos.senha}</FieldError>}
                        </InputGroup>

                        <InputGroup>
                            <label htmlFor="cadastro-perfil">Tipo de usuário</label>
                            {/* AU3: só ALUNO e PROFESSOR; ADMIN não existe no cadastro público */}
                            <Select
                                id="cadastro-perfil"
                                name="perfil"
                                value={dados.perfil}
                                onChange={handleChange}
                            >
                                <option value="ALUNO">Aluno</option>
                                <option value="PROFESSOR">Professor</option>
                            </Select>
                            {errosCampos.perfil && <FieldError>{errosCampos.perfil}</FieldError>}
                        </InputGroup>

                        {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}

                        <Button type="submit" disabled={enviando}>
                            {enviando ? "Cadastrando..." : "Cadastrar"}
                        </Button>

                        <LinkText>
                            Já tem conta? <Link to="/login">Entrar</Link>
                        </LinkText>
                    </Form>
                </RightSide>
            </Box>
        </Container>
    );
};

export default Register;
