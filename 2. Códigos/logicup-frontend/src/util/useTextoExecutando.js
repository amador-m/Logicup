import { useEffect, useState } from "react";

const DEMORA_MS = 4000; // um programa leva ~3 s; passou disso, quase sempre é fila

/*
 * Sprint 10: texto do botão "Executar" enquanto o programa roda.
 *
 * O executor roda 3 programas por vez. Numa turma inteira clicando junto, o pedido pode esperar até 30 s
 * na fila (logicup.executor.espera-na-fila). Sem aviso, parece que travou e a pessoa clica de novo.
 * Depois de 4 s, o botão passa a dizer que o programa está na fila.
 *
 * O "demorando" volta a false na limpeza do efeito (quando a execução termina), e não no corpo dele:
 * mudar estado direto no corpo de um useEffect faz o React desenhar a tela duas vezes à toa.
 */
export function useTextoExecutando(executando) {
    const [demorando, setDemorando] = useState(false);

    useEffect(() => {
        if (!executando) return undefined;
        const relogio = setTimeout(() => setDemorando(true), DEMORA_MS);
        return () => {
            clearTimeout(relogio);
            setDemorando(false);
        };
    }, [executando]);

    if (!executando) return "Executar";
    return demorando ? "Na fila, já vai..." : "Executando...";
}
