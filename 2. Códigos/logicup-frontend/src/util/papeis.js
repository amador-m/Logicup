/*
 * Sprint 7: quem vê o quê, do ponto de vista da INTERFACE (mostrar ou esconder links e telas).
 * A proteção REAL é sempre o backend (SecurityConfig): aqui é só experiência do usuário.
 */
export const PAPEIS_EMENTA = ["PROFESSOR", "ADMIN"];

/** true se o usuário logado pode ver a ementa (professor ou admin). */
export function podeVerEmenta(usuario) {
    return PAPEIS_EMENTA.includes(usuario?.role);
}

/** Sprint 9: só PROFESSOR gerencia turmas (admin fica de fora nesta versão — decisão A5). */
export function podeGerenciarTurmas(usuario) {
    return usuario?.role === "PROFESSOR";
}

/** Sprint 9: só ALUNO entra em turma. */
export function podeEntrarEmTurma(usuario) {
    return usuario?.role === "ALUNO";
}

/** Sprint 9: conquistas, gemas e compra de avatar são só do ALUNO (só ele tem progresso). */
export function podeVerConquistas(usuario) {
    return usuario?.role === "ALUNO";
}
