package br.com.logicup.backend.controller;

import br.com.logicup.backend.dto.AlterarSenhaRequest;
import br.com.logicup.backend.dto.AtualizarPerfilRequest;
import br.com.logicup.backend.dto.ExcluirContaRequest;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.security.EncerramentoDeSessao;
import br.com.logicup.backend.security.TokenService;
import br.com.logicup.backend.security.UsuarioLogado;
import br.com.logicup.backend.dto.TrocarAvatarRequest;
import br.com.logicup.backend.service.AvatarService;
import br.com.logicup.backend.service.ExclusaoContaService;
import br.com.logicup.backend.service.PerfilService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final AvatarService avatarService;
    private final ExclusaoContaService exclusaoConta;        // Sprint 10
    private final EncerramentoDeSessao encerramentoDeSessao; // Sprint 10
    private final TokenService tokenService;                 // Sprint 11: cookie novo depois de trocar a senha

    public PerfilController(PerfilService perfilService, AvatarService avatarService,
                            ExclusaoContaService exclusaoConta, EncerramentoDeSessao encerramentoDeSessao,
                            TokenService tokenService) {
        this.perfilService = perfilService;
        this.avatarService = avatarService;
        this.exclusaoConta = exclusaoConta;
        this.encerramentoDeSessao = encerramentoDeSessao;
        this.tokenService = tokenService;
    }

    /** Atualiza nome e username. Devolve o usuário atualizado (o frontend troca o "Olá, ..." na hora). */
    @PutMapping
    public UsuarioResponse atualizar(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody AtualizarPerfilRequest dados) {
        return perfilService.atualizar(UsuarioLogado.de(jwt).id(), dados);
    }

    /** Sprint 9: troca o avatar em uso por um que a pessoa possui (gratuito ou comprado). */
    @PutMapping("/avatar")
    public UsuarioResponse trocarAvatar(@AuthenticationPrincipal Jwt jwt,
                                        @Valid @RequestBody TrocarAvatarRequest dados) {
        return avatarService.trocar(UsuarioLogado.de(jwt), dados.codigo());
    }

    /**
     * Troca a senha. 204 = deu certo e não há nada para devolver.
     * Sprint 11: as OUTRAS sessões (outros aparelhos) são encerradas; esta continua porque a resposta traz um
     * cookie novo, com um token emitido depois da troca.
     */
    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(@AuthenticationPrincipal Jwt jwt,
                                             @Valid @RequestBody AlterarSenhaRequest dados,
                                             HttpServletResponse response) {
        String tokenNovo = perfilService.alterarSenha(UsuarioLogado.de(jwt).id(), dados);
        response.addHeader(HttpHeaders.SET_COOKIE, tokenService.criarCookie(tokenNovo).toString());
        return ResponseEntity.noContent().build();
    }

    /**
     * Sprint 10: exclui a PRÓPRIA conta, confirmando com a senha. 204 = excluída; a resposta já apaga o
     * cookie de login. POST (e não DELETE) porque leva um corpo (a senha), e DELETE com corpo não tem
     * significado definido no HTTP (alguns proxies e bibliotecas descartam o corpo).
     */
    @PostMapping("/exclusao")
    public ResponseEntity<Void> excluirConta(@AuthenticationPrincipal Jwt jwt,
                                             @Valid @RequestBody ExcluirContaRequest dados,
                                             HttpServletRequest request, HttpServletResponse response) {
        exclusaoConta.excluirComSenha(UsuarioLogado.de(jwt).id(), dados.senhaAtual());
        encerramentoDeSessao.encerrar(request, response);
        return ResponseEntity.noContent().build();
    }

    /** Sprint 10: conta só do Google (sem senha) -> manda o link de exclusão para o e-mail. 202 = pedido aceito. */
    @PostMapping("/exclusao/link")
    public ResponseEntity<Void> pedirLinkDeExclusao(@AuthenticationPrincipal Jwt jwt, HttpServletRequest request) {
        exclusaoConta.pedirLinkDeExclusao(UsuarioLogado.de(jwt).id(), request.getRemoteAddr());
        return ResponseEntity.accepted().build();
    }
}
