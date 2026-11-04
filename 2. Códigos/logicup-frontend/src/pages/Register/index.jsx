import { useState } from "react";
import {
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
    SuccessMessage,
} from "../Login/styles";
import LogoImg from "../../assets/images/LogoLight.png";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/useAuth";
import CaixaAceite from "../../components/ui/CaixaAceite";
import ReenviarConfirmacao from "../../components/ui/ReenviarConfirmacao";
import { VERSAO_TERMOS, VERSAO_PRIVACIDADE } from "../../data/legal/versoes";

// Sprint 8: aceiteTermos começa FALSO (a caixa nunca vem marcada)
const DADOS_INICIAIS = { nome: "", username: "", email: "", senha: "", perfil: "ALUNO", aceiteTermos: false };

/*
 * Sprint 7: o username só aceita letras minúsculas, números, ponto e sublinhado.
 * Enquanto a pessoa digita, convertemos para minúsculas e tiramos espaços — só para AJUDAR.
 * Quem GARANTE a regra é o backend (@Pattern no CadastroRequest + UNIQUE no banco).
 */
const ajustarUsername = (valor) => valor.toLowerCase().replace(/\s/g, "");

const Register = () => {
    const { cadastrar } = useAuth();

    const [dados, setDados] = useState(DADOS_INICIAIS);
    const [erro, setErro] = useState("");              // erro geral (ex.: e-mail já cadastrado)
    const [errosCampos, setErrosCampos] = useState({}); // erros por campo vindos do backend
    const [enviando, setEnviando] = useState(false);
    // Sprint 10: conta criada, esperando a confirmação do e-mail (guarda o e-mail para mostrar e reenviar)
    const [pendente, setPendente] = useState(null);

    // um único handler para todos os campos: usa o "name" do input para saber qual atualizar
    const handleChange = (event) => {
        const { name, value } = event.target;
        const valor = name === "username" ? ajustarUsername(value) : value;
        setDados((anteriores) => ({ ...anteriores, [name]: valor }));
    };

    const handleRegister = async (event) => {
        event.preventDefault();
        setErro("");
        setErrosCampos({});
        setEnviando(true);

        try {
            // Sprint 8: junto com o aceite vão as VERSÕES que a pessoa viu; o backend confere se são as vigentes
            // Sprint 10: a conta é criada, mas a pessoa só entra depois de abrir o link do e-mail
            const resposta = await cadastrar({ ...dados, versaoTermos: VERSAO_TERMOS, versaoPrivacidade: VERSAO_PRIVACIDADE });
            setPendente(resposta.email);
            setDados(DADOS_INICIAIS); // a senha não fica guardada na memória da tela
        } catch (error) {
            const campos = error.campos ?? {};
            setErrosCampos(campos);
            // se o erro já aparece embaixo de um campo, não repete a mesma mensagem no aviso geral
            setErro(Object.keys(campos).length > 0 ? "" : error.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        // Sprint 7: sem o <Container> de página inteira; o card agora aparece dentro do ModalAuth
        <Box>
                <LeftSide>
                    <Link to="/"><Logo src={LogoImg} alt="Logo LogiCup"/></Link>
                    <h2>Comece sua jornada em Java</h2>
                    <p>
                        Crie sua conta para acessar a trilha, concluir missões e evoluir
                        no aprendizado de programação
                    </p>
                </LeftSide>

                <RightSide>
                    {pendente ? (
                    <>
                    <h2 id="titulo-cadastro">Verifique seu e-mail</h2>
                    <p>Falta só um passo para começar a sua trilha.</p>
                    <SuccessMessage role="status">
                        Enviamos um link de confirmação para <strong>{pendente}</strong>. Abra o e-mail e clique em
                        "Confirmar meu e-mail": você entra direto na trilha. O link vale por 24 horas.
                    </SuccessMessage>
                    <p>Não chegou em alguns minutos? Confira o spam ou peça outro:</p>
                    <ReenviarConfirmacao emailInicial={pendente} idCampo="cadastro-reenviar-email" />
                    <LinkText>
                        Já confirmou? <Link to="/login">Entrar</Link>
                    </LinkText>
                    </>
                    ) : (
                    <>
                    <h2 id="titulo-cadastro">Criar conta</h2>
                    <p>Preencha seus dados para começar a aprender no LogiCup</p>

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
                            <label htmlFor="cadastro-username">Nome de usuário</label>
                            <input
                                id="cadastro-username"
                                name="username"
                                type="text"
                                placeholder="Ex.: aluno.dev"
                                autoComplete="username"
                                autoCapitalize="none"
                                spellCheck={false}
                                required
                                minLength={3}
                                maxLength={20}
                                pattern="[a-z0-9._]{3,20}"
                                aria-describedby="cadastro-username-regra"
                                value={dados.username}
                                onChange={handleChange}
                            />
                            {errosCampos.username ? (
                                <FieldError>{errosCampos.username}</FieldError>
                            ) : (
                                <small id="cadastro-username-regra">
                                    Aparece no ranking. De 3 a 20 caracteres: letras minúsculas, números, ponto ou sublinhado.
                                </small>
                            )}
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

                        <CaixaAceite
                            id="cadastro-aceite"
                            marcado={dados.aceiteTermos}
                            aoMudar={(marcado) => setDados((anteriores) => ({ ...anteriores, aceiteTermos: marcado }))}
                            erro={errosCampos.aceiteTermos}
                        />

                        {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}

                        <Button type="submit" disabled={enviando}>
                            {enviando ? "Cadastrando..." : "Cadastrar"}
                        </Button>

                        <LinkText>
                            Já tem conta? <Link to="/login">Entrar</Link>
                        </LinkText>
                    </Form>
                    </>
                    )}
                </RightSide>
        </Box>
    );
};

export default Register;
