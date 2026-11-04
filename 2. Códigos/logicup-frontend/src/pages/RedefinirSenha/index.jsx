import { useState } from "react";
import { Link } from "react-router-dom";
import { authApi, CODIGO_LINK_INVALIDO } from "../../services/api";
import { lerCodigoDoLink } from "../../util/codigoDoLink";
import PainelAuth from "../../components/ui/PainelAuth";
import { Form, InputGroup, Button, ErrorMessage, FieldError, SuccessMessage, LinkText } from "../Login/styles";

const LADO = {
    ladoTitulo: "Senha nova, mesma jornada",
    ladoTexto: "Seu progresso, XP e conquistas continuam guardados. Só a senha muda.",
};

/*
 * Sprint 10: página aberta pelo link de "esqueci minha senha" (/redefinir-senha#CODIGO).
 * Diferente da confirmação de e-mail, aqui o código só é enviado quando a pessoa digita a senha nova
 * (o link sozinho não faz nada). Depois, ela entra pelo login com a senha nova.
 */
const RedefinirSenha = () => {
    const [codigo] = useState(lerCodigoDoLink);
    const [senha, setSenha] = useState("");
    const [repeticao, setRepeticao] = useState("");
    const [enviando, setEnviando] = useState(false);
    const [concluido, setConcluido] = useState(false);
    const [linkInvalido, setLinkInvalido] = useState(!codigo);
    const [erro, setErro] = useState("");
    const [erroCampo, setErroCampo] = useState("");

    const salvar = async (evento) => {
        evento.preventDefault();
        setErro("");
        setErroCampo("");
        if (senha !== repeticao) {
            setErroCampo("As duas senhas não são iguais.");
            return;
        }
        setEnviando(true);
        try {
            await authApi.redefinirSenha(codigo, senha);
            setConcluido(true);
        } catch (error) {
            if (error.codigo === CODIGO_LINK_INVALIDO) {
                setLinkInvalido(true);
            } else {
                setErroCampo(error.campos?.novaSenha ?? "");
                setErro(error.campos?.novaSenha ? "" : error.message);
            }
        } finally {
            setEnviando(false);
        }
    };

    if (concluido) {
        return (
            <PainelAuth tituloId="titulo-redefinir" titulo="Senha alterada!" {...LADO}>
                <SuccessMessage role="status">Pronto! Agora é só entrar com a senha nova.</SuccessMessage>
                <LinkText><Link to="/login">Entrar</Link></LinkText>
            </PainelAuth>
        );
    }

    if (linkInvalido) {
        return (
            <PainelAuth tituloId="titulo-redefinir" titulo="Este link não vale mais"
                        subtitulo="Ele vale por 30 minutos e só pode ser usado uma vez. Peça um link novo e use o e-mail mais recente."
                        {...LADO}>
                <LinkText><Link to="/esqueci-senha">Pedir um link novo</Link></LinkText>
            </PainelAuth>
        );
    }

    return (
        <PainelAuth tituloId="titulo-redefinir" titulo="Crie uma senha nova"
                    subtitulo="Use pelo menos 8 caracteres. Uma frase curta é fácil de lembrar e difícil de adivinhar."
                    {...LADO}>
            <Form onSubmit={salvar}>
                <InputGroup>
                    <label htmlFor="redefinir-senha">Senha nova</label>
                    <input id="redefinir-senha" type="password" autoComplete="new-password" required
                           minLength={8} maxLength={72} value={senha}
                           onChange={(evento) => setSenha(evento.target.value)} />
                </InputGroup>
                <InputGroup>
                    <label htmlFor="redefinir-repeticao">Repita a senha nova</label>
                    <input id="redefinir-repeticao" type="password" autoComplete="new-password" required
                           minLength={8} maxLength={72} value={repeticao}
                           onChange={(evento) => setRepeticao(evento.target.value)} />
                    {erroCampo && <FieldError>{erroCampo}</FieldError>}
                </InputGroup>
                {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}
                <Button type="submit" disabled={enviando}>{enviando ? "Salvando..." : "Salvar senha nova"}</Button>
            </Form>
        </PainelAuth>
    );
};

export default RedefinirSenha;
