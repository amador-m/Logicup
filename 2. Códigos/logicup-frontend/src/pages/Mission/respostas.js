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
        case "TEXTO_LIVRE":
            return (resposta.texto ?? "").trim().length > 0;
        default:
            return false; // BLOCOS (X4) não é enviado
    }
}
