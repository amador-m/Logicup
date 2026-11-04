import { useState } from "react";
import { Link } from "react-router-dom";
import { authApi } from "../../services/api";
import PainelAuth from "../../components/ui/PainelAuth";
import { Form, InputGroup, Button, ErrorMessage, SuccessMessage, LinkText } from "../Login/styles";

/*
 * Sprint 10: "Esqueci minha senha". A pessoa informa o e-mail e recebe um link (vale 30 minutos).
 * A resposta é a MESMA exista ou não a conta: a tela nunca diz "e-mail não cadastrado", senão
 * qualquer um descobriria quem estuda no LogiCup testando e-mails aqui.
 */
const EsqueciSenha = () => {
    const [email, setEmail] = useState("");
    const [enviando, setEnviando] = useState(false);
    const [enviadoPara, setEnviadoPara] = useState("");
    const [erro, setErro] = useState("");

    const enviar = async (evento) => {
        evento.preventDefault();
        setErro("");
        setEnviando(true);
        try {
            await authApi.esqueciSenha(email.trim());
            setEnviadoPara(email.trim());
        } catch (error) {
            setErro(error.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <PainelAuth
            tituloId="titulo-esqueci"
            titulo="Esqueci minha senha"
            subtitulo={enviadoPara ? null : "Informe o e-mail da sua conta. Vamos enviar um link para você criar uma senha nova."}
            ladoTitulo="Acontece com todo mundo"
            ladoTexto="Seu progresso, XP e conquistas continuam guardados. Só a senha muda."
        >
            {enviadoPara ? (
                <>
                    <SuccessMessage role="status">
                        Se houver uma conta com o e-mail <strong>{enviadoPara}</strong>, enviamos um link para criar
                        uma senha nova. Ele vale por 30 minutos. Confira também a caixa de spam.
                    </SuccessMessage>
                    <LinkText><Link to="/login">Voltar para o login</Link></LinkText>
                </>
            ) : (
                <Form onSubmit={enviar}>
                    <InputGroup>
                        <label htmlFor="esqueci-email">E-mail</label>
                        <input
                            id="esqueci-email"
                            type="email"
                            placeholder="Digite o e-mail da sua conta"
                            autoComplete="email"
                            required
                            maxLength={150}
                            value={email}
                            onChange={(evento) => setEmail(evento.target.value)}
                        />
                    </InputGroup>
                    {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}
                    <Button type="submit" disabled={enviando}>{enviando ? "Enviando..." : "Enviar link"}</Button>
                    <LinkText>Lembrou? <Link to="/login">Entrar</Link></LinkText>
                </Form>
            )}
        </PainelAuth>
    );
};

export default EsqueciSenha;
