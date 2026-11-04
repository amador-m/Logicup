import { useEffect, useState } from "react";
import { authApi } from "../../../services/api";
import { Form, InputGroup, Button, ErrorMessage, SuccessMessage } from "../../../pages/Login/styles";

const ESPERA_SEGUNDOS = 60; // evita cliques repetidos (o backend também limita: 3 por hora por e-mail)

/*
 * Sprint 10: "Não recebi o e-mail". Usado no login (conta não confirmada), na tela de "verifique seu
 * e-mail" do cadastro e quando o link de confirmação venceu.
 *
 * A resposta do backend é SEMPRE a mesma (202), exista ou não a conta. Por isso a mensagem de sucesso
 * é condicional ("se houver uma conta..."): a tela não pode revelar quem está cadastrado.
 */
const ReenviarConfirmacao = ({ emailInicial = "", idCampo = "reenviar-email", textoBotao = "Reenviar link" }) => {
    const [email, setEmail] = useState(emailInicial);
    const [enviando, setEnviando] = useState(false);
    const [mensagem, setMensagem] = useState("");
    const [erro, setErro] = useState("");
    const [espera, setEspera] = useState(0);

    // contagem regressiva do botão: um "tique" por segundo enquanto espera > 0
    useEffect(() => {
        if (espera <= 0) return undefined;
        const relogio = setTimeout(() => setEspera((s) => s - 1), 1000);
        return () => clearTimeout(relogio);
    }, [espera]);

    const reenviar = async (evento) => {
        evento.preventDefault();
        setErro("");
        setMensagem("");
        setEnviando(true);
        try {
            await authApi.reenviarConfirmacao(email.trim());
            setMensagem(`Se houver uma conta aguardando confirmação para ${email.trim()}, um novo link acabou de ser enviado. `
                + "Confira também a caixa de spam. O link anterior deixou de valer.");
            setEspera(ESPERA_SEGUNDOS);
        } catch (error) {
            setErro(error.message); // ex.: 429 "Você já pediu vários e-mails em pouco tempo..."
        } finally {
            setEnviando(false);
        }
    };

    return (
        <Form onSubmit={reenviar}>
            <InputGroup>
                <label htmlFor={idCampo}>E-mail da conta</label>
                <input
                    id={idCampo}
                    type="email"
                    autoComplete="email"
                    required
                    maxLength={150}
                    value={email}
                    onChange={(evento) => setEmail(evento.target.value)}
                />
            </InputGroup>
            {mensagem && <SuccessMessage role="status">{mensagem}</SuccessMessage>}
            {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}
            <Button type="submit" disabled={enviando || espera > 0}>
                {enviando ? "Enviando..." : espera > 0 ? `Reenviar de novo em ${espera} s` : textoBotao}
            </Button>
        </Form>
    );
};

export default ReenviarConfirmacao;
