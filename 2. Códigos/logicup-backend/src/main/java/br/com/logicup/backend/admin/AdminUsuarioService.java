package br.com.logicup.backend.admin;

import br.com.logicup.backend.dto.PaginaResponse;
import br.com.logicup.backend.dto.UsuarioAdminResponse;
import br.com.logicup.backend.exception.RecursoNaoEncontradoException;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Sprint 11: GESTÃO DE USUÁRIOS pelo administrador (RF25). Decisões da equipe:
 *  - o admin LISTA e BUSCA usuários (nome, nome de usuário ou e-mail; filtro por perfil);
 *  - o admin BLOQUEIA e DESBLOQUEIA contas. Bloquear não apaga nada (progresso, XP e turmas ficam);
 *  - o admin NÃO vê senhas, NÃO troca senhas e NÃO mexe em XP;
 *  - ninguém bloqueia a si mesmo nem outro administrador (evita ficar sem nenhum admin).
 */
@Service
public class AdminUsuarioService {

    static final int TAMANHO_PAGINA = 20;
    private static final int TAMANHO_MAXIMO_BUSCA = 100;
    private static final Logger log = LoggerFactory.getLogger(AdminUsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public AdminUsuarioService(UsuarioRepository usuarioRepository, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioAdminResponse> listar(String busca, TipoUsuario perfil, int pagina) {
        if (pagina < 0) {
            throw new RequisicaoInvalidaException("Página inválida.");
        }
        // mais recentes primeiro; a ordem pelo id desempata e deixa a paginação estável
        PageRequest pedido = PageRequest.of(pagina, TAMANHO_PAGINA, Sort.by(Sort.Direction.DESC, "id"));
        return PaginaResponse.de(usuarioRepository.buscarParaAdmin(padraoDeBusca(busca), perfil, pedido),
                UsuarioAdminResponse::de);
    }

    @Transactional
    public UsuarioAdminResponse alterarBloqueio(Integer adminId, Integer usuarioId, boolean bloquear) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
        if (usuario.getId().equals(adminId)) {
            throw new RequisicaoInvalidaException("Você não pode bloquear a sua própria conta.");
        }
        if (usuario.getTipoUsuario() == TipoUsuario.ADMIN) {
            throw new RequisicaoInvalidaException("Contas de administrador não podem ser bloqueadas por aqui.");
        }
        if (bloquear) {
            usuario.bloquear(LocalDateTime.now(clock));
        } else {
            usuario.desbloquear();
        }
        // registro de auditoria: QUEM bloqueou QUEM (ids, sem dados pessoais no log)
        log.info("Admin {} {} a conta {}", adminId, bloquear ? "bloqueou" : "desbloqueou", usuarioId);
        return UsuarioAdminResponse.de(usuario);
    }

    /**
     * Texto digitado -> padrão do LIKE ("%texto%"), em minúsculas. Vazio -> null (= sem filtro).
     * Os caracteres especiais do LIKE (% e _) são "escapados": quem busca "a_b" procura exatamente "a_b",
     * e não "a" + qualquer letra + "b".
     */
    static String padraoDeBusca(String busca) {
        if (busca == null || busca.isBlank()) {
            return null;
        }
        String texto = busca.trim().toLowerCase(Locale.ROOT);
        if (texto.length() > TAMANHO_MAXIMO_BUSCA) {
            texto = texto.substring(0, TAMANHO_MAXIMO_BUSCA);
        }
        String escapado = texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
