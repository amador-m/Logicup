import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth";
import CaixaAceite from "../CaixaAceite";
import MascoteImg from "../../../assets/images/MascoteRecortado.png";
import { perfilApi } from "../../../services/api";
import {
    Dialogo, Mascote, Botoes, BotaoAceitar, BotaoSair, Mensagem, LinkExcluir, CampoSenha, BotaoExcluir, Sucesso,
} from "./styles";
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
 *
 * Sprint 10 (LGPD): uma terceira saída, "Prefiro excluir minha conta", para quem NÃO concorda com a versão
 * nova. A própria janela troca de conteúdo (não abre outra janela por cima): pede a senha, ou, para quem
 * entra só pelo Google, manda o link de exclusão para o e-mail. O backend libera esses dois pedidos mesmo
 * com o aceite pendente (AceitePendenteFilter).
 */

// nas páginas dos documentos a janela NÃO aparece: é lá que a pessoa lê o que vai aceitar.
// Sprint 10: nem na página do link de exclusão (a janela cobriria o botão de confirmar a exclusão).
const ROTAS_SEM_JANELA = ["/termos", "/privacidade", "/cookies", "/excluir-conta"];

const ModalAceite = () => {
    const { usuario } = useAuth();
    const { pathname } = useLocation();

    if (!usuario?.aceitePendente || ROTAS_SEM_JANELA.includes(pathname)) return null;
    return <JanelaAceite />;
};

const JanelaAceite = () => {
    const { usuario, aceitarDocumentos, logout } = useAuth();
    const navigate = useNavigate();
    const dialogoRef = useRef(null);

    const [marcado, setMarcado] = useState(false);
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState("");
    const [excluindo, setExcluindo] = useState(false);   // Sprint 10: mostrando a parte de excluir a conta
    const [senha, setSenha] = useState("");
    const [linkEnviado, setLinkEnviado] = useState(false);

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

    const trocarParaExclusao = (sim) => {
        setErro("");
        setSenha("");
        setExcluindo(sim);
    };

    const excluirConta = async (evento) => {
        evento.preventDefault();
        if (enviando) return;
        setErro("");
        setEnviando(true);
        try {
            if (usuario.temSenha) {
                await perfilApi.excluirConta(senha);
                // a página /excluir-conta mostra "Conta excluída" e é ela que esquece a pessoa logada
                navigate("/excluir-conta", { replace: true, state: { excluida: true } });
            } else {
                await perfilApi.pedirLinkDeExclusao();
                setLinkEnviado(true);
                setEnviando(false);
            }
        } catch (error) {
            setErro(error.campos?.senhaAtual ?? error.message);
            setEnviando(false);
        }
    };

    if (excluindo) {
        return (
            <Dialogo ref={dialogoRef} aria-labelledby="titulo-aceite" onCancel={(evento) => evento.preventDefault()}>
                <form onSubmit={excluirConta}>
                    <h2 id="titulo-aceite">Excluir sua conta</h2>
                    <p>
                        Apagamos a conta e tudo o que é dela: progresso, XP, gemas, conquistas, avatares e turmas.
                        Isso não pode ser desfeito.
                    </p>

                    {usuario.temSenha ? (
                        <CampoSenha htmlFor="aceite-senha-exclusao">
                            Digite a sua senha para confirmar
                            <input id="aceite-senha-exclusao" type="password" autoComplete="current-password" required
                                   value={senha} onChange={(e) => setSenha(e.target.value)} disabled={enviando} />
                        </CampoSenha>
                    ) : (
                        !linkEnviado && <p>Como você entra pelo Google, a confirmação é feita por um link enviado ao seu e-mail.</p>
                    )}

                    {linkEnviado && (
                        <Sucesso role="status">
                            ✓ Enviamos um link para {usuario.email}. Abra o e-mail e confirme em até 30 minutos.
                        </Sucesso>
                    )}
                    {erro && <Mensagem role="alert">{erro}</Mensagem>}

                    <Botoes>
                        {!linkEnviado && (
                            <BotaoExcluir type="submit" disabled={enviando || (usuario.temSenha && !senha)}>
                                {enviando ? "Aguarde..." : usuario.temSenha ? "Excluir para sempre" : "Enviar link de exclusão"}
                            </BotaoExcluir>
                        )}
                        <BotaoSair type="button" onClick={() => trocarParaExclusao(false)} disabled={enviando}>
                            Voltar
                        </BotaoSair>
                    </Botoes>
                </form>
            </Dialogo>
        );
    }

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
                {/* Sprint 10: quem não concorda com a versão nova pode ir embora levando os dados junto */}
                <LinkExcluir type="button" onClick={() => trocarParaExclusao(true)} disabled={enviando}>
                    Não concordo: prefiro excluir minha conta
                </LinkExcluir>
            </form>
        </Dialogo>
    );
};

export default ModalAceite;
