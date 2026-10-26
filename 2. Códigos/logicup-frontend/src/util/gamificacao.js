/*
 * Sprint 9: avisos para o app inteiro sobre a gamificação, usando eventos do navegador (CustomEvent).
 *
 * Por que eventos e não um Context?
 * Quem AVISA (a missão, a turma, a loja, o tutorial) e quem ESCUTA (Header, comemoração, tela de conquistas)
 * estão em partes bem diferentes da árvore de componentes. O evento liga os dois sem que um precise
 * importar o outro, e sem fazer a tela inteira renderizar de novo. É o mesmo padrão do "Ver tutorial".
 *
 * Dois avisos diferentes, de propósito:
 *  - PROGRESSO: "o saldo/medalhas podem ter mudado" -> o contador de gemas busca de novo (a cada questão).
 *  - COMEMORAR: "é uma boa hora para festa" -> a janela "Nova conquista!" pergunta se há medalha nova.
 *    Na missão isso só acontece na tela de RESULTADO da rodada: comemorar no meio de uma questão
 *    atrapalharia a concentração (a mesma lógica do Duolingo, que comemora no fim da lição).
 */
export const EVENTO_PROGRESSO = "logicup:progresso";
export const EVENTO_COMEMORAR = "logicup:comemorar";

export function avisarProgresso() {
    window.dispatchEvent(new Event(EVENTO_PROGRESSO));
}

export function pedirComemoracao() {
    window.dispatchEvent(new Event(EVENTO_COMEMORAR));
}

/** Registra quem escuta; devolve a função que cancela (para usar no retorno de um useEffect). */
export function ouvirProgresso(callback) {
    window.addEventListener(EVENTO_PROGRESSO, callback);
    return () => window.removeEventListener(EVENTO_PROGRESSO, callback);
}

export function ouvirComemoracao(callback) {
    window.addEventListener(EVENTO_COMEMORAR, callback);
    return () => window.removeEventListener(EVENTO_COMEMORAR, callback);
}
