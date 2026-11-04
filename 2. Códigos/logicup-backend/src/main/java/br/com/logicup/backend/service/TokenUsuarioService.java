package br.com.logicup.backend.service;

import br.com.logicup.backend.exception.LinkInvalidoException;
import br.com.logicup.backend.model.FinalidadeToken;
import br.com.logicup.backend.model.TokenUsuario;
import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.repository.TokenUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Sprint 10: cria e confere os códigos de uso único dos links enviados por e-mail.
 *
 * Como funciona:
 *  1. gerar(): sorteia 32 bytes com SecureRandom (gerador próprio para segurança, imprevisível),
 *     transforma em texto (Base64 "de URL", que só usa letras, números, - e _) e grava SÓ o hash SHA-256.
 *     O código em texto puro volta para quem chamou, para virar o link do e-mail, e não fica em lugar nenhum.
 *  2. consumir(): recebe o código do link, calcula o hash, procura no banco, confere finalidade, validade
 *     e se já foi usado, e marca como usado. Qualquer problema vira a MESMA mensagem ("link inválido ou
 *     expirado"): quem tenta adivinhar códigos não descobre se acertou um código vencido.
 *
 * 32 bytes = 256 bits de aleatoriedade: adivinhar um código válido é, na prática, impossível.
 * SHA-256 (e não BCrypt, como nas senhas) porque o código já é aleatório e longo: o BCrypt é lento de
 * propósito para proteger senhas FRACAS, escolhidas por pessoas. Aqui isso não é necessário.
 */
@Service
public class TokenUsuarioService {

    private static final int BYTES_DO_CODIGO = 32;
    /** Quanto tempo um código vencido ainda fica guardado antes da limpeza (ver apagarAntigos). */
    static final java.time.Duration GUARDA_DEPOIS_DE_VENCER = java.time.Duration.ofDays(30);

    private final TokenUsuarioRepository repository;
    private final Clock clock;
    private final SecureRandom aleatorio = new SecureRandom();

    public TokenUsuarioService(TokenUsuarioRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /**
     * Cria um código novo e INVALIDA os anteriores da mesma finalidade (só o link mais recente vale).
     * @return o código em texto puro, para montar o link (nunca é gravado)
     */
    @Transactional
    public String gerar(Usuario usuario, FinalidadeToken finalidade) {
        LocalDateTime agora = LocalDateTime.now(clock);
        aposentarPendentes(usuario, finalidade, agora); // só o link mais recente vale

        byte[] bytes = new byte[BYTES_DO_CODIGO];
        aleatorio.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        repository.save(new TokenUsuario(usuario, finalidade, hash(codigo), agora));
        return codigo;
    }

    /**
     * Confere o código e o marca como usado.
     * @return o usuário dono do código
     * @throws LinkInvalidoException se não existir, for de outra finalidade, já tiver sido usado ou estiver vencido
     */
    @Transactional
    public Usuario consumir(String codigo, FinalidadeToken finalidade) {
        if (codigo == null || codigo.isBlank() || codigo.length() > 100) {
            throw new LinkInvalidoException();
        }
        LocalDateTime agora = LocalDateTime.now(clock);
        TokenUsuario token = repository.buscarPorHash(hash(codigo.trim()))
                .filter(t -> t.getFinalidade() == finalidade)
                .filter(t -> t.valeEm(agora))
                .orElseThrow(LinkInvalidoException::new);
        token.usar(agora);
        // depois de usar um, os outros pendentes da mesma finalidade também deixam de valer
        aposentarPendentes(token.getUsuario(), finalidade, agora);
        return token.getUsuario();
    }

    /**
     * Sprint 10 (minimização de dados, LGPD): apaga os códigos vencidos há mais de 30 dias.
     * Depois de vencido, o código não serve para nada; os 30 dias de folga ajudam a investigar
     * um abuso recente (ex.: alguém pedindo muitos links para o e-mail de outra pessoa).
     * @return quantos registros foram apagados
     */
    @Transactional
    public int apagarAntigos() {
        return repository.apagarVencidosAntesDe(LocalDateTime.now(clock).minus(GUARDA_DEPOIS_DE_VENCER));
    }

    private void aposentarPendentes(Usuario usuario, FinalidadeToken finalidade, LocalDateTime agora) {
        // o Hibernate grava as mudanças no commit (entidades gerenciadas: "dirty checking")
        repository.buscarPendentes(usuario.getId(), finalidade).forEach(t -> t.aposentar(agora));
    }

    /** SHA-256 em hexadecimal (64 caracteres), o formato da coluna hash_token. */
    public static String hash(String codigo) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(codigo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 sempre existe na JVM", e);
        }
    }
}
