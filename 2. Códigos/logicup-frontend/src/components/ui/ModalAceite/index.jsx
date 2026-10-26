import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";
import CaixaAceite from "../CaixaAceite";
import MascoteImg from "../../../assets/images/MascoteRecortado.png";
import { Dialogo, Mascote, Botoes, BotaoAceitar, BotaoSair, Mensagem } from "./styles";
import { VERSAO_TERMOS, VERSAO_PRIVACIDADE } from "../../../data/legal/versoes";

/*
 * Sprint 8: janela de aceite para quem JÁ tem conta e ainda não aceitou a versão vigente
 * (contas criadas antes dos Termos existirem, ou quando os textos mudarem de versão).
 *
 * É BLOQUEANTE de propósito: o backend recusa o resto da API (403 ACEITE_PENDENTE) até o aceite,
 * então a tela mostra só as duas saídas possíveis: aceitar ou sair da conta.
 *  - Esc NÃO fecha (fechar sem escolher deixaria a pessoa numa página que não funciona);
 *  - os botões ficam desabilitados durante o envio (evita o duplo clique mandar dois aceites).
 * Usa o <dialog> nativo, como o ModalAuth: o navegador prende o foco e deixa o fundo inerte.
 */

// nas páginas dos documentos a janela NÃO aparece: é lá que a pessoa lê o que vai aceitar
const ROTAS_DOS_DOCUMENTOS = ["/termos", "/privacidade", "/cookies"];

const ModalAceite = () => {
    const { usuario } = useAuth();
    const { pathname } = useLocation();

    if (!usuario?.aceitePendente || ROTAS_DOS_DOCUMENTOS.includes(pathname)) return null;
    return <JanelaAceite />;
};

const JanelaAceite = () => {
    const { aceitarDocumentos, logout } = useAuth();
    const navigate = useNavigate();
    const dialogoRef = useRef(null);

    const [marcado, setMarcado] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState("");

    useEffect(() => {
        const dialogo = dialogoRef.current;
        if (dialogo && !dialogo.open) dialogo.showModal();
        const overflowAnterior = document.body.style.overflow;
        document.body.style.overflow = "hidden";
        return () => {
            document.body.style.overflow = overflowAnterior;
            if (dialogo?.open) dialogo.close();
        };
    }, []);

    const aceitar = async (evento) => {
        evento.preventDefault();
        if (!marcado || enviando) return;
        setErro("");
        setEnviando(true);
        try {
            await aceitarDocumentos(); // deu certo: o usuário muda e esta janela sai da tela
        } catch (error) {
            // 409 = os textos mudaram de versão enquanto a página estava aberta
            setErro(error.status === 409
                ? "Os documentos foram atualizados. Recarregue a página para ler a versão mais recente."
                : error.message);
            setEnviando(false);
        }
    };

    const sair = async () => {
        setEnviando(true);
        navigate("/", { replace: true }); // mesma ordem do Header: sai da página protegida antes
        await logout();
    };

    return (
        <Dialogo
            ref={dialogoRef}
            aria-labelledby="titulo-aceite"
            aria-describedby="texto-aceite"
            onCancel={(evento) => evento.preventDefault()}
        >
            <form onSubmit={aceitar}>
                <Mascote src={MascoteImg} alt="" />
                <h2 id="titulo-aceite">Antes de continuar</h2>
                <p id="texto-aceite">
                    Para continuar usando o LogiCup, leia os Termos de Uso e a Política de Privacidade
                    (versões atuais abaixo) e confirme.
                </p>
                <p className="versao">
                    Termos de Uso versão {VERSAO_TERMOS} · Política de Privacidade versão {VERSAO_PRIVACIDADE}
                </p>

                <CaixaAceite id="aceite-pendente" marcado={marcado} aoMudar={setMarcado} desabilitado={enviando} />

                {erro && <Mensagem role="alert">{erro}</Mensagem>}

                <Botoes>
                    <BotaoAceitar type="submit" disabled={!marcado || enviando}>
                        {enviando ? "Enviando..." : "Aceitar e continuar"}
                    </BotaoAceitar>
                    <BotaoSair type="button" onClick={sair} disabled={enviando}>Sair da conta</BotaoSair>
                </Botoes>
            </form>
        </Dialogo>
    );
};

export default ModalAceite;
