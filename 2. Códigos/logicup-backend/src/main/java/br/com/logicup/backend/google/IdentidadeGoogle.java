package br.com.logicup.backend.google;

/**
 * Sprint 10: o que o LogiCup aproveita da resposta do Google depois do login (OpenID Connect).
 *
 * @param sub             identificador FIXO da pessoa no Google. É ele que fica em usuario.google_id, e não o
 *                        e-mail: a pessoa pode trocar o e-mail da conta Google, o "sub" nunca muda
 * @param email           e-mail da conta Google (já normalizado: minúsculas, sem espaços)
 * @param emailVerificado o Google confirma que a pessoa é dona do e-mail? (claim "email_verified")
 * @param nome            nome exibido no Google (só sugestão para o cadastro; a pessoa pode mudar)
 */
public record IdentidadeGoogle(String sub, String email, boolean emailVerificado, String nome) {
}
