package br.com.logicup.backend.security;

import br.com.logicup.backend.model.TipoUsuario;
import org.springframework.security.oauth2.jwt.Jwt;

/** Quem fez a requisição, lido do TOKEN (nunca de parâmetros enviados pelo cliente -> evita IDOR). */
public record UsuarioLogado(Integer id, TipoUsuario role) {

    public static UsuarioLogado de(Jwt jwt) {
        return new UsuarioLogado(
                Integer.valueOf(jwt.getSubject()),
                TipoUsuario.valueOf(jwt.getClaimAsString(TokenService.CLAIM_ROLE)));
    }

    public boolean isAluno() {
        return role == TipoUsuario.ALUNO;
    }
}
