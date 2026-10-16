package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;

/** O que a API devolve sobre um usuário. Nunca contém a senha. */
public record UsuarioResponse(Integer id, String nome, String username, String email, TipoUsuario role) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getUsername(),
                usuario.getEmail(), usuario.getTipoUsuario());
    }
}
