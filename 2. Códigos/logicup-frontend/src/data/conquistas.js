/*
 * Sprint 9: como cada conquista APARECE (a regra de quando se ganha é do backend).
 *
 * Moedas: arte "Fases LogiCup" da Manuela/Davi, pasta assets/conquistas, nome <tipo>-<cor>.png.
 * F1-B (decisão da Maria): cinza = ainda não conquistada; laranja = bronze; verde = prata; amarelo = ouro.
 * Como "prata" é verde (não existe moeda prateada na arte), o NOME do nível sempre aparece escrito.
 * F2: o tipo de moeda de cada conquista.
 */
const arquivos = import.meta.glob("../assets/conquistas/*.png", { eager: true, import: "default" });

export const TIPO_DA_CONQUISTA = {
    PRIMEIRO_ACERTO: "order",
    RODADAS: "multichoice",
    XP: "complete",
    OFENSIVA: "lightning",
    ACERTO_DE_PRIMEIRA: "finderror",
    BOSS: "boss",
    FASES: "puzzle",
    TURMA: "associate",
};

export const COR_DO_NIVEL = { UNICA: "yellow", BRONZE: "orange", PRATA: "green", OURO: "yellow" };

export const NOME_DO_NIVEL = { UNICA: "Especial", BRONZE: "Bronze", PRATA: "Prata", OURO: "Ouro" };

/** Unidade da métrica, para "faltam 3 dias" etc. (singular, plural) */
export const UNIDADE_DA_CONQUISTA = {
    PRIMEIRO_ACERTO: ["questão", "questões"],
    RODADAS: ["rodada", "rodadas"],
    XP: ["XP", "XP"],
    OFENSIVA: ["dia", "dias"],
    ACERTO_DE_PRIMEIRA: ["acerto de primeira", "acertos de primeira"],
    BOSS: ["Boss", "Bosses"],
    FASES: ["fase", "fases"],
    TURMA: ["turma", "turmas"],
};

/**
 * URL da moeda de uma conquista.
 * @param nivel nível conquistado (UNICA/BRONZE/PRATA/OURO) ou null = ainda bloqueada (cinza)
 */
export function moedaDa(codigo, nivel) {
    const tipo = TIPO_DA_CONQUISTA[codigo] ?? "puzzle";
    const cor = nivel ? COR_DO_NIVEL[nivel] : "block";
    return arquivos[`../assets/conquistas/${tipo}-${cor}.png`] ?? arquivos[`../assets/conquistas/puzzle-${cor}.png`];
}

export function unidade(codigo, quantidade) {
    const [singular, plural] = UNIDADE_DA_CONQUISTA[codigo] ?? ["", ""];
    return quantidade === 1 ? singular : plural;
}
