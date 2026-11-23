package br.com.logicup.backend.dto;

import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;

import java.time.LocalDateTime;

/**
 * Sprint 11: um usuário como o ADMIN o vê na gestão de usuários.
 * Nada de senha ou hash; do Google, só "sim/não" (o identificador do Google não interessa ao admin).
 */
public record UsuarioAdminResponse(Integer id, String nome, String username, String email, TipoUsuario perfil,
                                   LocalDateTime dataCadastro, boolean emailConfirmado, boolean entraComGoogle,
                                   boolean temSenha, boolean bloqueado, LocalDateTime bloqueadoEm) {

    public static UsuarioAdminResponse de(Usuario usuario) {
        return new UsuarioAdminResponse(usuario.getId(), usuario.getNome(), usuario.getUsername(),
                usuario.getEmail(), usuario.getTipoUsuario(), usuario.getDataCadastro(),
                usuario.isEmailConfirmado(), usuario.getGoogleId() != null, usuario.temSenha(),
                usuario.isBloqueado(), usuario.getBloqueadoEm());
    }
}
