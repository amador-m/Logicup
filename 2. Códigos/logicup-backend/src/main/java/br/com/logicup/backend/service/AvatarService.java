package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.AvatarResponse;
import br.com.logicup.backend.dto.AvataresResponse;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.exception.AvatarJaPossuidoException;
import br.com.logicup.backend.exception.AvatarNaoPossuidoException;
import br.com.logicup.backend.exception.GemasInsuficientesException;
import br.com.logicup.backend.exception.RecursoNaoEncontradoException;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.model.Avatar;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.AlunoAvatarRepository;
import br.com.logicup.backend.repository.AlunoRepository;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sprint 9: avatares (catálogo, loja e troca do avatar em uso).
 *
 * Quem POSSUI um avatar: todo mundo possui os gratuitos; o aluno possui também os que comprou (aluno_avatar).
 * Professor e admin não têm gemas, então só usam os gratuitos.
 * O id sempre vem do TOKEN (Controller): ninguém compra ou troca avatar em nome de outra pessoa.
 */
@Service
public class AvatarService {

    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final AlunoAvatarRepository alunoAvatarRepository;
    private final Clock clock;

    public AvatarService(UsuarioRepository usuarioRepository, AlunoRepository alunoRepository,
                         AlunoAvatarRepository alunoAvatarRepository, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.alunoRepository = alunoRepository;
        this.alunoAvatarRepository = alunoAvatarRepository;
        this.clock = clock;
    }

    /** GET /api/avatares */
    @Transactional(readOnly = true)
    public AvataresResponse catalogo(UsuarioLogado logado) {
        Usuario usuario = buscar(logado.id());
        Set<Avatar> possui = possuidos(logado);
        Integer gemas = logado.isAluno() ? alunoRepository.saldoDeGemas(logado.id()) : null;

        return new AvataresResponse(gemas, usuario.getAvatar(), Arrays.stream(Avatar.values())
                .map(avatar -> new AvatarResponse(avatar, avatar.getNome(), avatar.getPreco(), avatar.isGratuito(),
                        possui.contains(avatar), avatar == usuario.getAvatar()))
                .toList());
    }

    /**
     * POST /api/avatares/{codigo}/compra (só ALUNO, garantido no SecurityConfig).
     * Ordem: 1) o avatar existe? (404)  2) já é dele? (409)  3) débito atômico (409 se faltar saldo)  4) grava a compra.
     * Tudo numa transação: se o passo 4 falhar (ex.: compra repetida simultânea), o débito do passo 3 é desfeito.
     */
    @Transactional
    public AvataresResponse comprar(UsuarioLogado logado, String codigo) {
        Avatar avatar = Avatar.porCodigo(codigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avatar não encontrado."));

        if (avatar.isGratuito() || alunoAvatarRepository.existsByIdUsuarioAndCodigoAvatar(logado.id(), avatar.name())) {
            throw new AvatarJaPossuidoException();
        }
        // o banco testa o saldo e debita NA MESMA instrução (sem janela para duas compras gastarem o mesmo saldo)
        if (alunoRepository.debitarGemas(logado.id(), avatar.getPreco()) == 0) {
            throw new GemasInsuficientesException(avatar.getPreco());
        }
        // se outra requisição já gravou esta compra, a chave primária recusa AQUI dentro,
        // a transação é desfeita (devolvendo as gemas) e o handler responde 409
        alunoAvatarRepository.registrarCompra(logado.id(), avatar.name(), avatar.getPreco(), LocalDateTime.now(clock));
        return catalogo(logado);
    }

    /** PUT /api/perfil/avatar (qualquer usuário logado): troca o avatar EM USO por um que a pessoa possui. */
    @Transactional
    public UsuarioResponse trocar(UsuarioLogado logado, String codigo) {
        Avatar avatar = Avatar.porCodigo(codigo)
                .orElseThrow(() -> new RequisicaoInvalidaException("Avatar inexistente."));
        if (!possuidos(logado).contains(avatar)) {
            throw new AvatarNaoPossuidoException();
        }
        Usuario usuario = buscar(logado.id());
        usuario.trocarAvatar(avatar); // dirty checking: o UPDATE sai no fim da transação
        return UsuarioResponse.de(usuario, false); // só chega aqui quem já aceitou (AceitePendenteFilter)
    }

    // ---------------------------------------------------------------- apoio

    private Set<Avatar> possuidos(UsuarioLogado logado) {
        Set<Avatar> possui = Arrays.stream(Avatar.values()).filter(Avatar::isGratuito).collect(Collectors.toSet());
        if (logado.isAluno()) {
            alunoAvatarRepository.findByIdUsuario(logado.id()).forEach(compra ->
                    Avatar.porCodigo(compra.getCodigoAvatar()).ifPresent(possui::add));
        }
        return possui;
    }

    private Usuario buscar(Integer id) {
        return usuarioRepository.findById(id).orElseThrow(SessaoInvalidaException::new);
    }
}
