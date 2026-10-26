/*
 * Sprint 8: versões VIGENTES dos documentos legais no frontend.
 *
 * Precisam ser IGUAIS às do backend (VersoesLegais.java). O cadastro e o aceite enviam estes números;
 * se forem diferentes dos do servidor, o backend responde 409 ("recarregue a página e leia de novo").
 * Ao publicar uma versão nova de um texto: troque o texto, a versão aqui e a do backend, no mesmo deploy.
 */
// Sprint 9: 0.2 — turmas (o professor passa a ver dados dos alunos da turma dele)
// Sprint 9: 0.3 — conquistas, gemas (moeda virtual) e avatares
export const VERSAO_TERMOS = "0.3";
export const VERSAO_PRIVACIDADE = "0.3";
export const VERSAO_COOKIES = "0.1";

/** Data da última atualização dos textos (aparece no topo de cada documento). */
export const ATUALIZADO_EM = "22 de outubro de 2026";
