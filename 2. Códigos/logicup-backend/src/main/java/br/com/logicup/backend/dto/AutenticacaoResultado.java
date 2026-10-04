package br.com.logicup.backend.dto;

/**
 * Resultado interno de cadastro/login: os dados do usuário (vão no corpo da resposta)
 * e o token (vai SOMENTE no cookie httpOnly — nunca no corpo).
 */
public record AutenticacaoResultado(UsuarioResponse usuario, String token) {
    @Override
    public String toString() { // o token nunca deve aparecer em log
        return "AutenticacaoResultado[usuario=" + usuario + "]";
    }
}
