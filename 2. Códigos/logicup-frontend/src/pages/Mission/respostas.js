/*
 * Sprint 9 (D3): quantas lacunas a questão RESPOSTA_CURTA tem.
 * Vem de alternativas = {"lacunas": 2} (público). 0 = formato de lacuna única ({"texto": ...}).
 */
export function quantidadeDeLacunas(exercicio) {
    const quantidade = exercicio.alternativas?.lacunas;
    return Number.isInteger(quantidade) && quantidade > 1 ? quantidade : 0;
}

/*
 * Quando uma questão conta como RESPONDIDA (e por isso entra no envio — T2 permite enviar só parte).
 * Evita mandar questões pela metade, que o backend corrigiria como erradas e contaria como tentativa (PR3).
 */
export function estaRespondida(exercicio, resposta) {
    if (!resposta) return false;

    switch (exercicio.formato) {
        case "ESCOLHA_UNICA":
            return Boolean(resposta.alternativa);
        case "ORDENACAO":
            return Array.isArray(resposta.ordem); // só existe depois que o aluno mexe na lista
        case "ASSOCIACAO":
            // todos os itens precisam estar associados
            return exercicio.alternativas.itens.every((item) => resposta.pares?.[item.id]);
        case "RESPOSTA_CURTA":
            // Sprint 9 (D3): com várias lacunas, TODAS precisam estar preenchidas
            if (quantidadeDeLacunas(exercicio) > 0) {
                return Array.isArray(resposta.lacunas)
                    && resposta.lacunas.length === quantidadeDeLacunas(exercicio)
                    && resposta.lacunas.every((lacuna) => (lacuna ?? "").trim().length > 0);
            }
            return (resposta.texto ?? "").trim().length > 0;
        case "TEXTO_LIVRE":
            return (resposta.texto ?? "").trim().length > 0;
        case "CODIGO": {
            // Sprint 9 (ADR-001): precisa ter escrito ALGO além do código inicial (que é só um comentário)
            const codigo = (resposta.codigo ?? "").trim();
            const inicial = (exercicio.alternativas?.codigoInicial ?? "").trim();
            return codigo.length > 0 && codigo !== inicial;
        }
        default:
            return false; // BLOCOS (X4) não é enviado
    }
}
