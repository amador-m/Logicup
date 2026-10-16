package br.com.logicup.backend.service;

import br.com.logicup.backend.dto.AutenticacaoResultado;
import br.com.logicup.backend.dto.CadastroRequest;
import br.com.logicup.backend.dto.LoginRequest;
import br.com.logicup.backend.dto.PerfilCadastro;
import br.com.logicup.backend.dto.UsuarioResponse;
import br.com.logicup.backend.exception.CredenciaisInvalidasException;
import br.com.logicup.backend.exception.EmailJaCadastradoException;
import br.com.logicup.backend.exception.SessaoInvalidaException;
import br.com.logicup.backend.exception.UsernameEmUsoException;
import br.com.logicup.backend.model.Aluno;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.security.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Regras de negócio de autenticação: cadastro, login e "quem sou eu".
 * O Controller só recebe/devolve HTTP; as decisões ficam aqui.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /*
     * Hash de uma senha qualquer, usado quando o e-mail NÃO existe no login.
     * Assim o BCrypt roda nos dois casos e o tempo de resposta fica parecido —
     * sem isso, dava para descobrir e-mails cadastrados medindo o tempo (reforça a AU7).
     */
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.hashFicticio = passwordEncoder.encode("logicup-senha-ficticia-para-tempo-constante");
    }

    /** "Ana@X.com " e "ana@x.com" são o mesmo e-mail (C4). */
    public static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    @Transactional
    public AutenticacaoResultado cadastrar(CadastroRequest dados) {
        String email = normalizarEmail(dados.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }
        // Sprint 7: checagem "amigável" (mensagem no campo). A garantia final é a UNIQUE do banco:
        // se dois cadastros com o mesmo username chegarem juntos, o banco recusa o segundo (409 pelo handler).
        if (usuarioRepository.existsByUsername(dados.username())) {
            throw new UsernameEmUsoException();
        }

        String nome = dados.nome().trim();
        String hash = passwordEncoder.encode(dados.senha()); // a senha em texto puro nunca é gravada

        // ALUNO ganha também a linha na tabela aluno (XP, streak, nível); PROFESSOR é só Usuario
        Usuario novo = (dados.perfil() == PerfilCadastro.ALUNO)
                ? new Aluno(nome, dados.username(), email, hash)
                : new Usuario(nome, dados.username(), email, hash, dados.perfil().paraTipoUsuario());

        Usuario salvo = usuarioRepository.save(novo);
        return autenticado(salvo); // C1: cadastro já deixa a pessoa logada
    }

    /**
     * Sprint 7: login com e-mail OU username no mesmo campo.
     * Tem "@" -> é e-mail (username não aceita "@"); senão -> é username. Os dois são normalizados
     * (minúsculas, sem espaços nas pontas), então "Ana.Silva" e "ana.silva" entram na mesma conta.
     */
    @Transactional(readOnly = true)
    public AutenticacaoResultado login(LoginRequest dados) {
        Optional<Usuario> usuario = dados.ehEmail()
                ? usuarioRepository.findByEmail(normalizarEmail(dados.login()))
                : usuarioRepository.findByUsername(CadastroRequest.normalizarUsername(dados.login()));

        String hashParaComparar = usuario.map(Usuario::getSenha).orElse(hashFicticio);
        boolean senhaConfere = passwordEncoder.matches(dados.senha(), hashParaComparar);

        if (usuario.isEmpty() || !senhaConfere) {
            throw new CredenciaisInvalidasException(); // mesma mensagem em todos os casos (AU7)
        }
        return autenticado(usuario.get());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarUsuarioLogado(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario)
                .map(UsuarioResponse::de)
                .orElseThrow(SessaoInvalidaException::new);
    }

    private AutenticacaoResultado autenticado(Usuario usuario) {
        return new AutenticacaoResultado(UsuarioResponse.de(usuario), tokenService.gerarToken(usuario));
    }
}
