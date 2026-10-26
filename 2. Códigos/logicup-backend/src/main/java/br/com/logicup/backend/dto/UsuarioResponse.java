package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.Avatar;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;

/**
 * O que a API devolve sobre um usuário. Nunca contém a senha.
 * Sprint 8: aceitePendente = true quando a sessão ainda não tem o aceite da versão vigente dos
 * Termos/Privacidade (o React mostra a janela de aceite e o backend bloqueia o resto da API).
 */
public record UsuarioResponse(Integer id, String nome, String username, String email, TipoUsuario role,
                              boolean aceitePendente, Avatar avatar) { // Sprint 9: avatar em uso

    public static UsuarioResponse de(Usuario usuario, boolean aceitePendente) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getUsername(),
                usuario.getEmail(), usuario.getTipoUsuario(), aceitePendente, usuario.getAvatar());
    }
}
