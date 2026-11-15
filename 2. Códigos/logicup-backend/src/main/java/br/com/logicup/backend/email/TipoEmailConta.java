package br.com.logicup.backend.email;

/** Sprint 10: os e-mails automáticos que o LogiCup envia. */
public enum TipoEmailConta {
    /** No cadastro (e no "reenviar"): link para confirmar o e-mail. Decisão D1-A: curto e com um objetivo só. */
    CONFIRMAR_EMAIL,
    /** No "Esqueci minha senha": link para criar uma senha nova. */
    REDEFINIR_SENHA,
    /** Depois de confirmar o e-mail: apresentação do Gattuccino e primeiros passos. */
    BOAS_VINDAS,
    /** Pedido de exclusão de conta de quem entra só pelo Google: link para confirmar (vale 30 minutos). */
    EXCLUIR_CONTA,
    /** Aviso depois que a conta foi excluída (se não foi a pessoa, ela fica sabendo). Sem link. */
    CONTA_EXCLUIDA
}
