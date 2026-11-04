import { useEffect, useId, useRef, useState } from "react";
import { Dialogo, Botoes, BotaoConfirmar, BotaoCancelar, Erro } from "../Confirmacao/styles";
import { problemaNoNome } from "../../../blocos/caixaDeFerramentas";

/*
 * Sprint 9 (ADR-001): as janelas que o Blockly pede (criar/renomear variável, confirmar, avisar),
 * no visual do LogiCup (mesmo estilo da Confirmacao) em vez do prompt()/confirm() do navegador.
 *
 * No "prompt", o nome é conferido AQUI, antes de chegar ao Blockly: nome de variável precisa ser válido
 * no Java (o Blockly aceitaria "minha variável", e o programa gerado não compilaria).
 */
const JanelaDoBlockly = ({ pedido, aoResponder }) => {
    const dialogoRef = useRef(null);
    const idTitulo = useId();
    const idCampo = useId();
    const [nome, setNome] = useState(pedido.padrao ?? "");
    const [erro, setErro] = useState("");

    useEffect(() => {
        const dialogo = dialogoRef.current;
        if (dialogo && !dialogo.open) dialogo.showModal();
        return () => { if (dialogo?.open) dialogo.close(); };
    }, []);

    const ehNome = pedido.tipo === "prompt";
    const cancelar = () => aoResponder(ehNome ? null : false);

    const confirmar = (evento) => {
        evento.preventDefault();
        if (pedido.tipo === "alert") return aoResponder();
        if (!ehNome) return aoResponder(true);
        const problema = problemaNoNome(nome.trim());
        if (problema) return setErro(problema);
        aoResponder(nome.trim());
    };

    const titulo = ehNome ? "Nome da variável" : pedido.tipo === "confirm" ? "Tem certeza?" : "Atenção";

    return (
        <Dialogo ref={dialogoRef} aria-labelledby={idTitulo} onCancel={(e) => { e.preventDefault(); cancelar(); }}>
            <form onSubmit={confirmar}>
                <h2 id={idTitulo}>{titulo}</h2>
                <p>{pedido.mensagem}</p>
                {ehNome && (
                    <label htmlFor={idCampo}>
                        Use letras sem acento, sem espaço (ex.: <strong>pontos</strong>, <strong>totalDeBaus</strong>)
                        <input id={idCampo} value={nome} autoFocus autoComplete="off" spellCheck={false}
                               onChange={(e) => { setNome(e.target.value); setErro(""); }} />
                    </label>
                )}
                {erro && <Erro role="alert">{erro}</Erro>}
                <Botoes>
                    {pedido.tipo !== "alert" && <BotaoCancelar type="button" onClick={cancelar}>Cancelar</BotaoCancelar>}
                    <BotaoConfirmar type="submit">{ehNome ? "Criar" : "OK"}</BotaoConfirmar>
                </Botoes>
            </form>
        </Dialogo>
    );
};

export default JanelaDoBlockly;
