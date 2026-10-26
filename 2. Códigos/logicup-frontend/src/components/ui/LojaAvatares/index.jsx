import { useEffect, useRef, useState } from "react";
import { useAuth } from "../../../context/useAuth";
import { avatarApi, perfilApi } from "../../../services/api";
import { avisarProgresso } from "../../../util/gamificacao";
import { podeVerConquistas } from "../../../util/papeis";
import AvatarUsuario from "../AvatarUsuario";
import IconeGema from "../Gemas/IconeGema";
import {
    Dialogo, Topo, Saldo, Fechar, Corpo, Mensagem, Grade, Item, Selo, Preco, Acao, Dica,
} from "./styles";

/*
 * Sprint 9: loja e escolha de avatar (decisão F3), numa janela nativa (<dialog>), como o onboarding.
 * Abre no Perfil ("Trocar avatar") e na tela de Conquistas ("Gastar gemas").
 *
 * Quem decide TUDO é o backend: se possui, se tem saldo, o preço. A tela só mostra o que ele responde
 * e reage aos erros (409 sem gemas, 403 sem o avatar). Comprar pede uma confirmação: gema gasta não volta.
 */
const LojaAvatares = ({ aoFechar }) => {
    const { usuario, atualizarUsuario } = useAuth();
    const dialogoRef = useRef(null);
    const tituloRef = useRef(null);
    const [catalogo, setCatalogo] = useState(null);
    const [confirmando, setConfirmando] = useState(null); // código do avatar aguardando "Sim, comprar"
    const [recemComprado, setRecemComprado] = useState(null);
    const [ocupado, setOcupado] = useState(false);
    const [mensagem, setMensagem] = useState(null); // { tipo: "erro" | "ok", texto }

    const aluno = podeVerConquistas(usuario);

    useEffect(() => {
        const dialogo = dialogoRef.current;
        if (dialogo && !dialogo.open) dialogo.showModal();
        tituloRef.current?.focus();
        let vivo = true;
        avatarApi.catalogo()
            .then((dados) => { if (vivo) setCatalogo(dados); })
            .catch(() => { if (vivo) setMensagem({ tipo: "erro", texto: "Não foi possível carregar os avatares. Tente de novo." }); });
        return () => {
            vivo = false;
            if (dialogo?.open) dialogo.close();
        };
    }, []);

    const comprar = async (avatar) => {
        setOcupado(true);
        setMensagem(null);
        try {
            const dados = await avatarApi.comprar(avatar.codigo);
            setCatalogo(dados);
            setRecemComprado(avatar.codigo);
            setMensagem({ tipo: "ok", texto: `${avatar.nome} desbloqueado! Clique em "Usar" para equipar.` });
            avisarProgresso(); // o contador de gemas do Header atualiza
        } catch (erro) {
            setMensagem({ tipo: "erro", texto: erro.message });
            avatarApi.catalogo().then(setCatalogo).catch(() => {}); // o saldo pode ter mudado
        } finally {
            setConfirmando(null);
            setOcupado(false);
        }
    };

    const usar = async (avatar) => {
        setOcupado(true);
        setMensagem(null);
        try {
            const atualizado = await perfilApi.trocarAvatar(avatar.codigo);
            atualizarUsuario(atualizado); // Header, perfil e trilha trocam o avatar na hora
            setCatalogo((atual) => ({
                ...atual,
                avatarAtual: avatar.codigo,
                avatares: atual.avatares.map((a) => ({ ...a, emUso: a.codigo === avatar.codigo })),
            }));
            setMensagem({ tipo: "ok", texto: `Agora você é o(a) ${avatar.nome}!` });
        } catch (erro) {
            setMensagem({ tipo: "erro", texto: erro.message });
        } finally {
            setOcupado(false);
        }
    };

    const acao = (avatar) => {
        if (avatar.emUso) return <Acao type="button" disabled>Em uso</Acao>;
        if (avatar.possui) {
            return <Acao type="button" $variante="usar" disabled={ocupado} onClick={() => usar(avatar)}>Usar</Acao>;
        }
        if (!aluno) return <Dica>Exclusivo para alunos (com gemas)</Dica>;
        const faltam = avatar.preco - (catalogo.gemas ?? 0);
        if (faltam > 0) return <Dica>Faltam {faltam} gemas</Dica>;
        if (confirmando === avatar.codigo) {
            return (
                <>
                    <Acao type="button" $variante="confirmar" disabled={ocupado} onClick={() => comprar(avatar)}>
                        Sim, gastar {avatar.preco}
                    </Acao>
                    <Acao type="button" disabled={ocupado} onClick={() => setConfirmando(null)}>Cancelar</Acao>
                </>
            );
        }
        return <Acao type="button" $variante="comprar" disabled={ocupado} onClick={() => setConfirmando(avatar.codigo)}>Comprar</Acao>;
    };

    return (
        <Dialogo ref={dialogoRef} aria-labelledby="titulo-loja"
                 onCancel={(evento) => { evento.preventDefault(); aoFechar(); }}>
            <Topo>
                <div>
                    <h2 id="titulo-loja" ref={tituloRef} tabIndex={-1}>Avatares</h2>
                    <p>{aluno ? "Ganhe gemas com conquistas e desbloqueie novos visuais." : "Escolha como você aparece no LogiCup."}</p>
                </div>
                {aluno && catalogo && (
                    <Saldo aria-label={`Saldo: ${catalogo.gemas} gemas`}><IconeGema /><strong>{catalogo.gemas}</strong></Saldo>
                )}
                <Fechar type="button" onClick={aoFechar} aria-label="Fechar">✕</Fechar>
            </Topo>

            <Corpo>
                {/* aria-live: o leitor de tela anuncia "desbloqueado" ou o erro sem a pessoa procurar */}
                <div aria-live="polite">
                    {mensagem && <Mensagem $tipo={mensagem.tipo}>{mensagem.texto}</Mensagem>}
                </div>

                {!catalogo && !mensagem && <p>Carregando avatares...</p>}

                {catalogo && (
                    <Grade>
                        {catalogo.avatares.map((avatar) => (
                            <Item key={avatar.codigo} $emUso={avatar.emUso}
                                  className={`${avatar.possui ? "" : "bloqueado"} ${recemComprado === avatar.codigo ? "novo" : ""}`}>
                                {avatar.emUso && <Selo $cor="var(--green)">Em uso</Selo>}
                                {recemComprado === avatar.codigo && !avatar.emUso && <Selo $cor="var(--orange)">Novo!</Selo>}
                                <AvatarUsuario codigo={avatar.codigo} tamanho={92} borda={3} />
                                <strong>{avatar.nome}</strong>
                                {!avatar.possui && (
                                    <Preco aria-label={`${avatar.preco} gemas`}><IconeGema $tamanho={16} />{avatar.preco}</Preco>
                                )}
                                {avatar.possui && avatar.gratuito && <Dica>Grátis</Dica>}
                                {avatar.possui && !avatar.gratuito && <Dica>Desbloqueado</Dica>}
                                {acao(avatar)}
                            </Item>
                        ))}
                    </Grade>
                )}
            </Corpo>
        </Dialogo>
    );
};

export default LojaAvatares;
