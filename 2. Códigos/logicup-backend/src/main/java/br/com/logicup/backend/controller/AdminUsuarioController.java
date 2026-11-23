package br.com.logicup.backend.controller;

import br.com.logicup.backend.admin.AdminUsuarioService;
import br.com.logicup.backend.dto.BloqueioRequest;
import br.com.logicup.backend.dto.PaginaResponse;
import br.com.logicup.backend.dto.UsuarioAdminResponse;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.security.UsuarioLogado;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 11: gestão de usuários. Só ADMIN chega aqui (SecurityConfig: /api/admin/** -> hasRole ADMIN).
 * O id de quem está agindo vem do TOKEN (para impedir que o admin bloqueie a si mesmo).
 */
@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminUsuarioController {

    private final AdminUsuarioService adminUsuarioService;

    public AdminUsuarioController(AdminUsuarioService adminUsuarioService) {
        this.adminUsuarioService = adminUsuarioService;
    }

    /** GET /api/admin/usuarios?busca=ana&perfil=ALUNO&pagina=0 (todos opcionais). */
    @GetMapping
    public PaginaResponse<UsuarioAdminResponse> listar(@RequestParam(required = false) String busca,
                                                       @RequestParam(required = false) TipoUsuario perfil,
                                                       @RequestParam(defaultValue = "0") int pagina) {
        return adminUsuarioService.listar(busca, perfil, pagina);
    }

    /** PUT (e não POST): "deixe a conta neste estado" — repetir o mesmo pedido não muda nada (idempotente). */
    @PutMapping("/{id}/bloqueio")
    public UsuarioAdminResponse alterarBloqueio(@AuthenticationPrincipal Jwt jwt, @PathVariable Integer id,
                                                @Valid @RequestBody BloqueioRequest dados) {
        return adminUsuarioService.alterarBloqueio(UsuarioLogado.de(jwt).id(), id, dados.bloqueado());
    }
}
