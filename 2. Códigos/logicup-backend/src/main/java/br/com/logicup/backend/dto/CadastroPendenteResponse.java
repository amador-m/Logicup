package br.com.logicup.backend.dto;

/**
 * Sprint 10 (decisão C): resposta do cadastro. A conta foi criada, mas a pessoa ainda NÃO está logada:
 * primeiro ela confirma o e-mail pelo link. O frontend usa o e-mail para mostrar
 * "enviamos um link para fulano@..." e para o botão "reenviar".
 */
public record CadastroPendenteResponse(String email, String mensagem) {

    public static CadastroPendenteResponse para(String email) {
        return new CadastroPendenteResponse(email,
                "Conta criada! Enviamos um link de confirmação para o seu e-mail. Abra o link para entrar.");
    }
}
