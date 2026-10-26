import { useEffect, useId, useRef, useState } from "react";
import { Dialogo, Botoes, BotaoConfirmar, BotaoCancelar, Erro } from "./styles";

/*
 * Sprint 9: janela de confirmação para ações que não têm volta (remover aluno, excluir turma, sair da turma).
 * Usa o <dialog> nativo, como o ModalAuth: foco preso dentro dela, Esc cancela, fundo inerte.
 * Substitui o window.confirm(), que não pode ser estilizado e trava a página inteira.
 *
 *  exigirTexto: quando informado, o botão só habilita depois que a pessoa digitar exatamente esse texto
 *               (usado para excluir a turma: digitar o nome dela — R14).
 *  aoConfirmar: função async; se der erro, a mensagem aparece aqui e a janela continua aberta.
 */
const Confirmacao = ({ titulo, mensagem, textoConfirmar, exigirTexto, aoConfirmar, aoCancelar }) => {
    const dialogoRef = useRef(null);
    const idTitulo = useId();
    const idCampo = useId();
    const [digitado, setDigitado] = useState("");
    const [enviando, setEnviando] = useState(false);
    const [erro, setErro] = useState("");

    useEffect(() => {
        const dialogo = dialogoRef.current;
        if (dialogo && !dialogo.open) dialogo.showModal();
        return () => {
            if (dialogo?.open) dialogo.close();
        };
    }, []);

    const liberado = !exigirTexto || digitado.trim() === exigirTexto.trim();

    const confirmar = async (evento) => {
        evento.preventDefault();
        if (!liberado || enviando) return;
        setErro("");
        setEnviando(true);
        try {
            await aoConfirmar();
        } catch (e) {
            setErro(e.message);
            setEnviando(false);
        }
    };

    return (
        <Dialogo
            ref={dialogoRef}
            aria-labelledby={idTitulo}
            onCancel={(evento) => { evento.preventDefault(); if (!enviando) aoCancelar(); }}
        >
            <form onSubmit={confirmar}>
                <h2 id={idTitulo}>{titulo}</h2>
                <p>{mensagem}</p>

                {exigirTexto && (
                    <label htmlFor={idCampo}>
                        Para confirmar, digite <strong>{exigirTexto}</strong>
                        <input id={idCampo} value={digitado} onChange={(e) => setDigitado(e.target.value)}
                               autoComplete="off" disabled={enviando} />
                    </label>
                )}

                {erro && <Erro role="alert">{erro}</Erro>}

                <Botoes>
                    <BotaoCancelar type="button" onClick={aoCancelar} disabled={enviando}>Cancelar</BotaoCancelar>
                    <BotaoConfirmar type="submit" disabled={!liberado || enviando}>
                        {enviando ? "Aguarde..." : textoConfirmar}
                    </BotaoConfirmar>
                </Botoes>
            </form>
        </Dialogo>
    );
};

export default Confirmacao;
