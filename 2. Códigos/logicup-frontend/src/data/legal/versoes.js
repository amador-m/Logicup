/*
 * Sprint 8: versões VIGENTES dos documentos legais no frontend.
 *
 * Precisam ser IGUAIS às do backend (VersoesLegais.java). O cadastro e o aceite enviam estes números;
 * se forem diferentes dos do servidor, o backend responde 409 ("recarregue a página e leia de novo").
 * Ao publicar uma versão nova de um texto: troque o texto, a versão aqui e a do backend, no mesmo deploy.
 */
// Sprint 9: 0.2 — turmas (o professor passa a ver dados dos alunos da turma dele)
// Sprint 9: 0.3 — conquistas, gemas (moeda virtual) e avatares
// Sprint 10: 0.4 — confirmação de e-mail, recuperação de senha, e-mails pela Brevo, hospedagem e canal de contato
// Sprint 10: 0.5 — entrar com o Google e exclusão da conta pelo perfil (Política de Cookies 0.2: cookies do Google)
export const VERSAO_TERMOS = "0.5";
export const VERSAO_PRIVACIDADE = "0.5";
export const VERSAO_COOKIES = "0.2";

/** Data da última atualização dos textos (aparece no topo de cada documento). */
export const ATUALIZADO_EM = "15 de novembro de 2026";
