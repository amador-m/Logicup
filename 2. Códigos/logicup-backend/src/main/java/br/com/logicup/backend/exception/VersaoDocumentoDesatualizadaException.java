package br.com.logicup.backend.exception;

/** Sprint 8: a pessoa leu uma versão antiga dos Termos ou da Política (página aberta antes de uma atualização). */
public class VersaoDocumentoDesatualizadaException extends RuntimeException {

    public VersaoDocumentoDesatualizadaException() {
        super("Os Termos de Uso ou a Política de Privacidade foram atualizados. Recarregue a página e leia a versão atual.");
    }
}
