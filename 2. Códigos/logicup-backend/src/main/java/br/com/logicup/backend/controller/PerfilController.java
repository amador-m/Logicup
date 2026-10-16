package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.AlterarSenhaRequest;
import br.com.logicup.backend.dto.AtualizarPerfilRequest;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.service.PerfilService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sprint 7: edição do perfil de quem está logado (qualquer tipo de usuário).
 * Exige login pela regra "anyRequest().authenticated()" do SecurityConfig; PUT exige também o token CSRF.
 * Para LER o perfil, o frontend usa o GET /api/auth/me que já existe.
 *
 * PUT (e não PATCH): o cliente envia o recurso editável INTEIRO (nome + username).
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    /** Atualiza nome e username. Devolve o usuário atualizado (o frontend troca o "Olá, ..." na hora). */
    @PutMapping
    public UsuarioResponse atualizar(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody AtualizarPerfilRequest dados) {
        return perfilService.atualizar(UsuarioLogado.de(jwt).id(), dados);
    }

    /** Troca a senha. 204 = deu certo e não há nada para devolver. A sessão atual continua válida. */
    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(@AuthenticationPrincipal Jwt jwt,
                                             @Valid @RequestBody AlterarSenhaRequest dados) {
        perfilService.alterarSenha(UsuarioLogado.de(jwt).id(), dados);
        return ResponseEntity.noContent().build();
    }
}
