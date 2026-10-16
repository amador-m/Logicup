package br.com.logicup.backend.exception;

/**
 * Sprint 7: a senha atual informada na troca de senha não confere -> HTTP 400 (e NÃO 401).
 * Motivo: a pessoa CONTINUA logada (o token é válido); só um campo do formulário está errado.
 * Um 401 faria o frontend achar que a sessão expirou e deslogar a pessoa.
 */
public class SenhaAtualIncorretaException extends RuntimeException {
    public SenhaAtualIncorretaException() {
        super("A senha atual está incorreta.");
    }
}
