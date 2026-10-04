import { catalogoFases } from "../data/catalogoFases";

/** Status possíveis de uma fase NO MAPA (I9). */
export const STATUS_FASE = {
    CONCLUIDA: "completed",
    DISPONIVEL: "available",
    BLOQUEADA: "locked",
    EM_BREVE: "soon",
};

/**
 * I9: o status da fase é DERIVADO das rodadas (o backend só calcula status de rodada).
 *  - todas aprovadas      -> concluída
 *  - alguma liberada      -> disponível
 *  - todas bloqueadas     -> bloqueada
 * Uma fase sem rodadas é tratada como bloqueada (conteúdo incompleto).
 */
export function statusDaFase(rodadas) {
    if (rodadas.length > 0 && rodadas.every((rodada) => rodada.status === "APROVADA")) {
        return STATUS_FASE.CONCLUIDA;
    }
    if (rodadas.some((rodada) => rodada.status !== "BLOQUEADA")) {
        return STATUS_FASE.DISPONIVEL;
    }
    return STATUS_FASE.BLOQUEADA;
}

/**
 * Junta o catálogo visual (10 fases) com as fases reais da API, pelo NÚMERO da fase (I7).
 * I1: fase que não veio da API vira "Em breve".  I8: fase real mostra os títulos das rodadas como tópicos.
 */
export function montarMapa(fasesDaApi) {
    const porOrdem = new Map(fasesDaApi.map((fase) => [fase.ordem, fase]));

    return catalogoFases.map((itemCatalogo) => {
        const fase = porOrdem.get(itemCatalogo.ordem);

        if (!fase) {
            return { ...itemCatalogo, existe: false, status: STATUS_FASE.EM_BREVE };
        }

        return {
            ordem: fase.ordem,
            titulo: fase.titulo,
            boss: itemCatalogo.boss ?? false,
            topicos: fase.rodadas.map((rodada) => rodada.titulo),
            existe: true,
            status: statusDaFase(fase.rodadas),
            xpGanho: somar(fase.rodadas, "xpGanho"),
            xpTotal: somar(fase.rodadas, "xpTotal"),
            exerciciosConcluidos: somar(fase.rodadas, "exerciciosConcluidos"),
            totalExercicios: somar(fase.rodadas, "totalExercicios"),
        };
    });
}

function somar(lista, campo) {
    return lista.reduce((total, item) => total + item[campo], 0);
}

/** "Maria Eduarda Amador" -> "MA" (avatar). */
export function iniciais(nome = "") {
    const partes = nome.trim().split(/\s+/).filter(Boolean);
    if (partes.length === 0) return "?";
    const primeira = partes[0][0];
    const ultima = partes.length > 1 ? partes[partes.length - 1][0] : "";
    return (primeira + ultima).toUpperCase();
}
