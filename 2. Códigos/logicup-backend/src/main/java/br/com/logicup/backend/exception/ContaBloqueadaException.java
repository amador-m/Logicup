package br.com.logicup.backend.exception;

/**
 * Sprint 11: conta bloqueada pelo administrador tentando entrar -> HTTP 403 com código CONTA_BLOQUEADA.
 * Só aparece para quem ACERTOU a senha (ou provou quem é pelo Google ou por um link de e-mail):
 * quem está chutando senhas não descobre que a conta existe.
 */
public class ContaBloqueadaException extends RuntimeException {

    public static final String CODIGO = "CONTA_BLOQUEADA";

    public ContaBloqueadaException() {
        super("Esta conta está bloqueada. Se achar que é um engano, escreva para contato@logicup.dev.br.");
    }
}
