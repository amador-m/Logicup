package br.com.logicup.backend.exception;

/** Sprint 9 (loja): o saldo de gemas não cobre o preço do avatar -> HTTP 409. */
public class GemasInsuficientesException extends RuntimeException {
    public GemasInsuficientesException(int preco) {
        super("Gemas insuficientes: este avatar custa " + preco + " gemas. Ganhe mais conquistas e volte aqui!");
    }
}
