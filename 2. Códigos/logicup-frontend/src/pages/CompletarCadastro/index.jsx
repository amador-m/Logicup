import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/useAuth";
import { authApi } from "../../services/api";
import { VERSAO_TERMOS, VERSAO_PRIVACIDADE } from "../../data/legal/versoes";
import PainelAuth from "../../components/ui/PainelAuth";
import CaixaAceite from "../../components/ui/CaixaAceite";
import { Form, InputGroup, Button, Select, ErrorMessage, FieldError, LinkText, BotaoGoogle } from "../Login/styles";

const LADO = {
    ladoTitulo: "Quase lá!",
    ladoTexto: "O Google já confirmou o seu e-mail. Falta só escolher como você vai aparecer no LogiCup.",
};

/* mesma ajuda do cadastro normal: minúsculas e sem espaços (quem garante a regra é o backend) */
const ajustarUsername = (valor) => valor.toLowerCase().replace(/\s/g, "");

/*
 * Sprint 10 (decisão D3): primeira vez de alguém que entrou pelo Google. O backend guardou "quem o Google
 * disse que é" num cookie assinado por 10 minutos; aqui a pessoa escolhe o nome de usuário e o perfil e
 * aceita os Termos. O e-mail aparece só para conferência: ele vem do Google e não pode ser trocado.
 */
const CompletarCadastro = () => {
    const { completarCadastroGoogle } = useAuth();
    const navigate = useNavigate();
    const [google, setGoogle] = useState(null);          // { nome, email }
    const [expirou, setExpirou] = useState(false);
    const [dados, setDados] = useState({ nome: "", username: "", perfil: "ALUNO", aceiteTermos: false });
    const [erro, setErro] = useState("");
    const [errosCampos, setErrosCampos] = useState({});
    const [enviando, setEnviando] = useState(false);

    useEffect(() => {
        let ativo = true;
        authApi.googlePendente()
            .then((resposta) => {
                if (!ativo) return;
                setGoogle(resposta);
                setDados((d) => ({ ...d, nome: resposta.nome ?? "" }));
            })
            .catch(() => { if (ativo) setExpirou(true); });
        return () => { ativo = false; };
    }, []);

    const mudar = (evento) => {
        const { name, value } = evento.target;
        setDados((d) => ({ ...d, [name]: name === "username" ? ajustarUsername(value) : value }));
    };

    const enviar = async (evento) => {
        evento.preventDefault();
        setErro("");
        setErrosCampos({});
        setEnviando(true);
        try {
            await completarCadastroGoogle({ ...dados, versaoTermos: VERSAO_TERMOS, versaoPrivacidade: VERSAO_PRIVACIDADE });
            navigate("/trilha", { replace: true });
        } catch (error) {
            if (error.status === 401) { setExpirou(true); return; }
            const campos = error.campos ?? {};
            setErrosCampos(campos);
            setErro(Object.keys(campos).length > 0 ? "" : error.message);
        } finally {
            setEnviando(false);
        }
    };

    if (expirou) {
        return (
            <PainelAuth tituloId="titulo-completar" titulo="O tempo acabou"
                        subtitulo="Por segurança, esta etapa vale por 10 minutos. Entre com o Google de novo para continuar."
                        {...LADO}>
                <BotaoGoogle href="/oauth2/authorization/google">Entrar com Google</BotaoGoogle>
                <LinkText><Link to="/cadastro">Prefiro me cadastrar com e-mail e senha</Link></LinkText>
            </PainelAuth>
        );
    }

    if (!google) {
        return <PainelAuth tituloId="titulo-completar" titulo="Carregando..." {...LADO} />;
    }

    return (
        <PainelAuth tituloId="titulo-completar" titulo="Complete seu cadastro"
                    subtitulo={`Você entrou com o Google como ${google.email}.`} {...LADO}>
            <Form onSubmit={enviar}>
                <InputGroup>
                    <label htmlFor="completar-nome">Nome</label>
                    <input id="completar-nome" name="nome" autoComplete="name" required minLength={2} maxLength={150}
                           value={dados.nome} onChange={mudar} />
                    {errosCampos.nome && <FieldError>{errosCampos.nome}</FieldError>}
                </InputGroup>
                <InputGroup>
                    <label htmlFor="completar-username">Nome de usuário</label>
                    <input id="completar-username" name="username" autoCapitalize="none" spellCheck={false} required
                           minLength={3} maxLength={20} pattern="[a-z0-9._]{3,20}" placeholder="Ex.: aluno.dev"
                           aria-describedby="completar-username-regra" value={dados.username} onChange={mudar} />
                    {errosCampos.username ? <FieldError>{errosCampos.username}</FieldError> : (
                        <small id="completar-username-regra">
                            Aparece no ranking. De 3 a 20 caracteres: letras minúsculas, números, ponto ou sublinhado.
                        </small>
                    )}
                </InputGroup>
                <InputGroup>
                    <label htmlFor="completar-perfil">Tipo de usuário</label>
                    <Select id="completar-perfil" name="perfil" value={dados.perfil} onChange={mudar}>
                        <option value="ALUNO">Aluno</option>
                        <option value="PROFESSOR">Professor</option>
                    </Select>
                </InputGroup>
                <CaixaAceite id="completar-aceite" marcado={dados.aceiteTermos}
                             aoMudar={(marcado) => setDados((d) => ({ ...d, aceiteTermos: marcado }))}
                             erro={errosCampos.aceiteTermos} />
                {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}
                <Button type="submit" disabled={enviando}>{enviando ? "Criando a conta..." : "Criar conta"}</Button>
            </Form>
        </PainelAuth>
    );
};

export default CompletarCadastro;
