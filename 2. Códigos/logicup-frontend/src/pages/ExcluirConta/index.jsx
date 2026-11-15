import { useEffect, useState } from "react";
import styled from "styled-components";
import { Link, useLocation } from "react-router-dom";
import { authApi, CODIGO_LINK_INVALIDO } from "../../services/api";
import { useAuth } from "../../context/useAuth";
import { lerCodigoDoLink } from "../../util/codigoDoLink";
import PainelAuth from "../../components/ui/PainelAuth";
import { Button, ErrorMessage, SuccessMessage, LinkText } from "../Login/styles";

/* o botão verde do login significa "seguir em frente"; aqui a ação é destrutiva, então é laranja (cor de alerta) */
const BotaoExcluir = styled(Button)`
  background: var(--orange);

  &:hover:not(:disabled) {
    background: var(--dark);
  }
`;

const LADO = {
    ladoTitulo: "Até a próxima xícara",
    ladoTexto: "Se um dia quiser voltar a aprender Java com a gente, é só criar uma conta nova.",
};

/*
 * Sprint 10: exclusão de conta.
 *  - /excluir-conta#CODIGO: aberta pelo link do e-mail (conta que entra só pelo Google);
 *  - /excluir-conta com state.excluida: para onde o Perfil manda depois de excluir com a senha.
 *
 * Abrir o link NÃO exclui sozinho: a pessoa precisa clicar no botão. Motivo: alguns provedores de e-mail
 * e antivírus "visitam" os links de um e-mail para checar se são seguros; se abrir já excluísse,
 * essa visita automática apagaria a conta sem a pessoa decidir.
 */
const ExcluirConta = () => {
    const location = useLocation();
    const { esquecerUsuario } = useAuth();
    const [codigo] = useState(lerCodigoDoLink);
    const [excluida, setExcluida] = useState(Boolean(location.state?.excluida));
    const [linkInvalido, setLinkInvalido] = useState(!codigo && !location.state?.excluida);
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState("");

    // veio do Perfil depois de excluir com a senha: o backend já apagou o cookie; aqui o React esquece a pessoa
    const veioDoPerfil = Boolean(location.state?.excluida);
    useEffect(() => {
        if (veioDoPerfil) esquecerUsuario();
    }, [veioDoPerfil, esquecerUsuario]);

    const excluir = async () => {
        setErro("");
        setEnviando(true);
        try {
            await authApi.excluirContaPorLink(codigo);
            esquecerUsuario(); // se este navegador estava logado nela, o backend já apagou o cookie
            setExcluida(true);
        } catch (error) {
            if (error.codigo === CODIGO_LINK_INVALIDO) setLinkInvalido(true);
            else setErro(error.message);
        } finally {
            setEnviando(false);
        }
    };

    if (excluida) {
        return (
            <PainelAuth tituloId="titulo-excluir-conta" titulo="Conta excluída" {...LADO}>
                <SuccessMessage role="status">
                    Pronto. A sua conta e todo o progresso dela foram apagados. Enviamos uma confirmação para o seu e-mail.
                </SuccessMessage>
                <LinkText><Link to="/">Voltar ao início</Link></LinkText>
            </PainelAuth>
        );
    }

    if (linkInvalido) {
        return (
            <PainelAuth tituloId="titulo-excluir-conta" titulo="Este link não vale mais"
                        subtitulo="Ele vale por 30 minutos e só pode ser usado uma vez. Entre na sua conta e peça um link novo no Perfil."
                        {...LADO}>
                <LinkText><Link to="/login">Entrar</Link></LinkText>
            </PainelAuth>
        );
    }

    return (
        <PainelAuth tituloId="titulo-excluir-conta" titulo="Excluir sua conta?"
                    subtitulo="Vamos apagar a conta e tudo o que é dela: progresso, XP, gemas, conquistas, avatares e turmas. Isso não pode ser desfeito."
                    {...LADO}>
            {erro && <ErrorMessage role="alert">{erro}</ErrorMessage>}
            <BotaoExcluir type="button" onClick={excluir} disabled={enviando}>
                {enviando ? "Excluindo..." : "Excluir minha conta para sempre"}
            </BotaoExcluir>
            <LinkText>Mudou de ideia? <Link to="/">Manter minha conta</Link></LinkText>
        </PainelAuth>
    );
};

export default ExcluirConta;
