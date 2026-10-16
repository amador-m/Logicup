package br.com.logicup.backend;

import br.com.logicup.backend.model.TipoUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.UsuarioRepository;
import br.com.logicup.backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prepara o ambiente de DESENVOLVIMENTO (D5: só roda com o perfil "dev").
 *
 * PE2 (Sprint 6): cria apenas o ADMIN de desenvolvimento, porque ADMIN não pode ser
 * criado pelo cadastro público. ALUNO e PROFESSOR de teste são criados pela própria
 * aplicação (tela de cadastro). E-mail e senha vêm das variáveis ADMIN_EMAIL e ADMIN_SENHA:
 * a senha não fica no código e é gravada com BCrypt, nunca em texto puro.
 *
 * (Até a Sprint 5 este runner criava "Prof. Carlos", "Ana Souza" e uma turma só para testar
 *  o mapeamento de Turma/AlunoTurma; esse teste agora é o TurmaAlunoTurmaRepositoryTest.)
 */
@Component
@Profile("dev")
public class DatabaseTestRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseTestRunner.class);
    private static final int SENHA_MINIMA = 8;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminSenha;

    public DatabaseTestRunner(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                              @Value("${logicup.dev.admin-email}") String adminEmail,
                              @Value("${logicup.dev.admin-senha}") String adminSenha) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = AuthService.normalizarEmail(adminEmail);
        this.adminSenha = adminSenha;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (adminEmail == null || adminEmail.isBlank() || adminSenha == null || adminSenha.isBlank()) {
            log.warn("ADMIN_EMAIL/ADMIN_SENHA não configurados: o ADMIN de desenvolvimento NÃO foi criado.");
            return;
        }
        if (adminSenha.length() < SENHA_MINIMA) {
            log.warn("ADMIN_SENHA precisa ter pelo menos {} caracteres: o ADMIN NÃO foi criado.", SENHA_MINIMA);
            return;
        }
        if (usuarioRepository.existsByEmail(adminEmail)) {
            log.info("ADMIN de desenvolvimento já existe ({}), nada a fazer.", adminEmail);
            return;
        }

        // Sprint 7: todo usuário tem username; o ADMIN de dev fica com "admin" (ou "admin_dev", se já existir)
        String username = usuarioRepository.existsByUsername("admin") ? "admin_dev" : "admin";
        Usuario admin = new Usuario("Administrador (dev)", username, adminEmail,
                passwordEncoder.encode(adminSenha), TipoUsuario.ADMIN);
        usuarioRepository.save(admin);
        log.info("ADMIN de desenvolvimento criado: {}", adminEmail);
    }
}
