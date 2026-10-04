import { useState } from "react";
import { OrderList, OrderItem, OrderControls, VisuallyHidden } from "../../styles";

/*
 * ORDENACAO (I5) — alternativas [{id, texto}] | resposta {"ordem": ["c", "b", "d", "a"]}
 * Botões ↑ e ↓ em vez de arrastar: funcionam com teclado, leitor de tela e toque no celular.
 * Enquanto o aluno não mexe, a lista aparece na ordem do banco (que já vem embaralhada).
 */
const Ordenacao = ({ exercicio, valor, aoMudar, desabilitado }) => {
    const [anuncio, setAnuncio] = useState(""); // lido pelo leitor de tela após cada movimento

    const porId = new Map(exercicio.alternativas.map((alternativa) => [alternativa.id, alternativa]));
    const ordemAtual = valor?.ordem ?? exercicio.alternativas.map((alternativa) => alternativa.id);

    const mover = (posicao, direcao) => {
        const destino = posicao + direcao;
        const novaOrdem = [...ordemAtual];
        [novaOrdem[posicao], novaOrdem[destino]] = [novaOrdem[destino], novaOrdem[posicao]]; // troca os dois
        aoMudar({ ordem: novaOrdem });
        setAnuncio(`${porId.get(novaOrdem[destino]).texto} agora está na posição ${destino + 1}.`);
    };

    return (
        <>
            <OrderList>
                {ordemAtual.map((id, posicao) => {
                    const texto = porId.get(id).texto;
                    return (
                        <OrderItem key={id}>
                            <strong>{posicao + 1}</strong>
                            <span>{texto}</span>
                            <OrderControls>
                                <button
                                    type="button"
                                    aria-label={`Mover "${texto}" para cima`}
                                    disabled={desabilitado || posicao === 0}
                                    onClick={() => mover(posicao, -1)}
                                >
                                    ↑
                                </button>
                                <button
                                    type="button"
                                    aria-label={`Mover "${texto}" para baixo`}
                                    disabled={desabilitado || posicao === ordemAtual.length - 1}
                                    onClick={() => mover(posicao, 1)}
                                >
                                    ↓
                                </button>
                            </OrderControls>
                        </OrderItem>
                    );
                })}
            </OrderList>
            <VisuallyHidden aria-live="polite">{anuncio}</VisuallyHidden>
        </>
    );
};

export default Ordenacao;
