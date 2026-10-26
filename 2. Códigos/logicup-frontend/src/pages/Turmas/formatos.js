/*
 * Sprint 9: formatação usada nas telas de turma.
 * Datas chegam do backend como "2026-10-30" (LocalDate, sem hora nem fuso).
 * NÃO usamos new Date("2026-10-30"): o navegador leria como meia-noite UTC e, no Brasil (UTC-3),
 * mostraria o dia ANTERIOR. Por isso a data é montada a partir do texto.
 */
export function formatarData(texto) {
    if (!texto) return null;
    const [ano, mes, dia] = texto.split("-");
    return `${dia}/${mes}/${ano}`;
}

/** Hoje no formato do <input type="date"> ("AAAA-MM-DD"), no fuso do navegador. */
export function hojeIso() {
    const agora = new Date();
    const doisDigitos = (n) => String(n).padStart(2, "0");
    return `${agora.getFullYear()}-${doisDigitos(agora.getMonth() + 1)}-${doisDigitos(agora.getDate())}`;
}

export const ROTULO_STATUS_TAREFA = {
    PENDENTE: "Pendente",
    CONCLUIDA: "Concluída",
    BLOQUEADA: "Bloqueada",
};

/** "1 aluno" / "3 alunos" */
export function plural(quantidade, singular, pluralTexto) {
    return `${quantidade} ${quantidade === 1 ? singular : pluralTexto}`;
}

/** Copia para a área de transferência. Devolve false se o navegador não permitir. */
export async function copiar(texto) {
    try {
        await navigator.clipboard.writeText(texto);
        return true;
    } catch {
        return false;
    }
}
