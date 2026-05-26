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
    Logo
} from "../Login/styles";
import LogoImg from "../../assets/images/LogoLight.png";

const Register = () => {
    return (
        <Container>
            <Box>
                <LeftSide>
                    <Logo src={LogoImg} alt="Logo Logicup" />
                    <h2>Comece sua jornada em Java</h2>
                    <p>
                        Crie sua conta para acessar a trilha, concluir missões e evoluir
                        no aprendizado de programação
                    </p>
                </LeftSide>

                <RightSide>
                    <h2>Criar conta</h2>
                    <p>Preencha seus dados para começar a aprender no Logicup</p>

                    <Form>
                        <InputGroup>
                            <label>Nome</label>
                            <input type="text" placeholder="Digite seu nome" />
                        </InputGroup>

                        <InputGroup>
                            <label>E-mail</label>
                            <input type="email" placeholder="Digite seu e-mail" />
                        </InputGroup>

                        <InputGroup>
                            <label>Senha</label>
                            <input type="password" placeholder="Crie uma senha" />
                        </InputGroup>

                        <InputGroup>
                            <label>Tipo de usuário</label>
                            <Select>
                                <option>Aluno</option>
                                <option>Professor</option>
                            </Select>
                        </InputGroup>

                        <Button>Cadastrar</Button>

                        <LinkText>
                            Já tem conta? <span>Entrar</span>
                        </LinkText>
                    </Form>
                </RightSide>
            </Box>
        </Container>
    );
};

export default Register;