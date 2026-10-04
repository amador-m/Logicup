import { MatchRow } from "../../styles";

/*
 * ASSOCIACAO (I5) — alternativas {"itens": [{id, texto}], "opcoes": [{id, texto}]}
 *                   resposta {"pares": {"1": "a", "2": "b"}}  (item -> opção)
 * Um <select> nativo por item: acessível e usa o seletor do próprio celular.
 */
const Associacao = ({ exercicio, valor, aoMudar, desabilitado }) => {
    const { itens, opcoes } = exercicio.alternativas;
    const pares = valor?.pares ?? {};

    const escolher = (itemId, opcaoId) => {
        const novosPares = { ...pares };
        if (opcaoId) {
            novosPares[itemId] = opcaoId;
        } else {
            delete novosPares[itemId]; // voltou para "Escolha..."
        }
        aoMudar({ pares: novosPares });
    };

    return (
        <div>
            {itens.map((item) => {
                const idCampo = `assoc-${exercicio.id}-${item.id}`;
                return (
                    <MatchRow key={item.id}>
                        <label htmlFor={idCampo}>{item.texto}</label>
                        <select
                            id={idCampo}
                            value={pares[item.id] ?? ""}
                            disabled={desabilitado}
                            onChange={(evento) => escolher(item.id, evento.target.value)}
                        >
                            <option value="">Escolha...</option>
                            {opcoes.map((opcao) => (
                                <option key={opcao.id} value={opcao.id}>
                                    {opcao.texto}
                                </option>
                            ))}
                        </select>
                    </MatchRow>
                );
            })}
        </div>
    );
};

export default Associacao;
