import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/useAuth";
import { CODIGO_LINK_INVALIDO } from "../../services/api";
import { lerCodigoDoLink } from "../../util/codigoDoLink";
import PainelAuth from "../../components/ui/PainelAuth";
import ReenviarConfirmacao from "../../components/ui/ReenviarConfirmacao";
import { ErrorMessage, LinkText } from "../Login/styles";

/*
 * Cada código só funciona UMA vez. Em desenvolvimento o StrictMode roda os efeitos duas vezes; sem esta
 * "memória" do módulo, o 2º envio voltaria "link inválido" logo depois do 1º ter dado certo.
 * Guardamos a PROMESSA do pedido: a segunda montagem espera o MESMO pedido em vez de fazer outro.
 */
const pedidos = new Map();

/*
 * Sprint 10: página aberta pelo link do e-mail de confirmação (/confirmar-email#CODIGO).
 * Deu certo -> o backend já devolve o cookie de login e a pessoa vai direto para a trilha.
 */
const ConfirmarEmail = () => {
    const { confirmarEmail } = useAuth();
    const navigate = useNavigate();
    const [codigo] = useState(lerCodigoDoLink);
    const [erro, setErro] = useState(codigo ? null : { semCodigo: true });

    useEffect(() => {
        if (!codigo) return undefined;
        let ativo = true;
        if (!pedidos.has(codigo)) {
            pedidos.set(codigo, confirmarEmail(codigo));
        }
        pedidos.get(codigo)
            .then(() => { if (ativo) navigate("/trilha", { replace: true }); })
            .catch((error) => { if (ativo) setErro(error); });
        return () => { ativo = false; };
    }, [codigo, confirmarEmail, navigate]);

    if (!erro) {
        return (
            <PainelAuth tituloId="titulo-confirmar" titulo="Confirmando seu e-mail..."
                        subtitulo="Só um instante: já vamos te levar para a trilha."
                        ladoTitulo="Quase lá!" ladoTexto="Confirmar o e-mail protege a sua conta e o seu progresso." />
        );
    }

    const linkRuim = erro.semCodigo || erro.codigo === CODIGO_LINK_INVALIDO;
    return (
        <PainelAuth
            tituloId="titulo-confirmar"
            titulo={linkRuim ? "Este link não vale mais" : "Não foi possível confirmar"}
            subtitulo={linkRuim
                ? "Ele pode ter vencido (vale por 24 horas), já ter sido usado, ou você pediu um link mais novo. "
                  + "Se o seu e-mail já foi confirmado, é só entrar. Se não, peça um link novo."
                : null}
            ladoTitulo="Quase lá!"
            ladoTexto="Confirmar o e-mail protege a sua conta e o seu progresso."
        >
            {!linkRuim && <ErrorMessage role="alert">{erro.message}</ErrorMessage>}
            <ReenviarConfirmacao textoBotao="Enviar um link novo" />
            <LinkText>
                Já confirmou? <Link to="/login">Entrar</Link>
            </LinkText>
        </PainelAuth>
    );
};

export default ConfirmarEmail;
