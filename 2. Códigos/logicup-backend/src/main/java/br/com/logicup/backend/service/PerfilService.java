package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.AlterarSenhaRequest;
import br.com.logicup.backend.dto.AtualizarPerfilRequest;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.exception.NovaSenhaIgualException;
import br.com.logicup.backend.exception.RequisicaoInvalidaException;
import br.com.logicup.backend.exception.SenhaAtualIncorretaException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.exception.UsernameEmUsoException;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Sprint 7: regras de edição do PRÓPRIO perfil (nome, username e senha). O e-mail não é editável.
 * O id SEMPRE vem do token (Controller), nunca do JSON: ninguém consegue editar o perfil de outra pessoa (IDOR).
 */
@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService; // Sprint 11: token novo depois de trocar a senha
    private final Clock clock;

    public PerfilService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                         TokenService tokenService, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    @Transactional
    public UsuarioResponse atualizar(Integer idUsuario, AtualizarPerfilRequest dados) {
        Usuario usuario = buscar(idUsuario);

        // só checa se MUDOU: manter o próprio username não é "username em uso"
        boolean trocouUsername = !dados.username().equals(usuario.getUsername());
        if (trocouUsername && usuarioRepository.existsByUsername(dados.username())) {
            throw new UsernameEmUsoException(); // 409 no campo username (mesma regra do cadastro)
        }

        usuario.setNome(dados.nome());
        usuario.setUsername(dados.username());
        /*
         * Não precisa chamar save(): dentro de uma transação, o Hibernate "acompanha" a entidade
         * carregada (dirty checking) e gera o UPDATE sozinho no commit.
         * saveAndFlush força o UPDATE AGORA: se dois usuários pegarem o mesmo username ao mesmo tempo,
         * a UNIQUE do banco recusa aqui dentro e o handler devolve 409.
         */
        usuarioRepository.saveAndFlush(usuario);
        return UsuarioResponse.de(usuario, false); // só chega aqui quem já aceitou (AceitePendenteFilter)
    }

    /**
     * Sprint 11: devolve um TOKEN NOVO. Trocar a senha encerra todas as sessões abertas (inclusive a atual, cujo
     * token foi emitido antes da troca); o controller põe este token novo no cookie, e assim só QUEM trocou a
     * senha, neste aparelho, continua logado. Só chega aqui quem já aceitou os Termos (AceitePendenteFilter),
     * por isso o token já sai com o aceite.
     */
    @Transactional
    public String alterarSenha(Integer idUsuario, AlterarSenhaRequest dados) {
        Usuario usuario = buscar(idUsuario);

        // Sprint 10: conta criada pelo Google não tem senha (não há "senha atual" para conferir)
        if (!usuario.temSenha()) {
            throw new RequisicaoInvalidaException("Sua conta entra pelo Google e ainda não tem senha. Para criar uma, "
                    + "saia e use \"Esqueci minha senha\" na tela de login, com o seu e-mail.");
        }
        if (!passwordEncoder.matches(dados.senhaAtual(), usuario.getSenha())) {
            throw new SenhaAtualIncorretaException();
        }
        if (passwordEncoder.matches(dados.novaSenha(), usuario.getSenha())) {
            throw new NovaSenhaIgualException();
        }
        usuario.trocarSenha(passwordEncoder.encode(dados.novaSenha()), LocalDateTime.now(clock)); // grava só o hash
        return tokenService.gerarToken(usuario, true);
    }

    private Usuario buscar(Integer idUsuario) {
        // token válido de um usuário que não existe mais (ex.: apagado no banco) -> 401
        return usuarioRepository.findById(idUsuario).orElseThrow(SessaoInvalidaException::new);
    }
}
