package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.AlterarSenhaRequest;
import br.com.logicup.backend.dto.AtualizarPerfilRequest;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.exception.NovaSenhaIgualException;
import br.com.logicup.backend.exception.SenhaAtualIncorretaException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.exception.UsernameEmUsoException;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sprint 7: regras de edição do PRÓPRIO perfil (nome, username e senha). O e-mail não é editável.
 * O id SEMPRE vem do token (Controller), nunca do JSON: ninguém consegue editar o perfil de outra pessoa (IDOR).
 */
@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public PerfilService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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
        return UsuarioResponse.de(usuario);
    }

    @Transactional
    public void alterarSenha(Integer idUsuario, AlterarSenhaRequest dados) {
        Usuario usuario = buscar(idUsuario);

        if (!passwordEncoder.matches(dados.senhaAtual(), usuario.getSenha())) {
            throw new SenhaAtualIncorretaException();
        }
        if (passwordEncoder.matches(dados.novaSenha(), usuario.getSenha())) {
            throw new NovaSenhaIgualException();
        }
        usuario.setSenha(passwordEncoder.encode(dados.novaSenha())); // grava só o hash
    }

    private Usuario buscar(Integer idUsuario) {
        // token válido de um usuário que não existe mais (ex.: apagado no banco) -> 401
        return usuarioRepository.findById(idUsuario).orElseThrow(SessaoInvalidaException::new);
    }
}
