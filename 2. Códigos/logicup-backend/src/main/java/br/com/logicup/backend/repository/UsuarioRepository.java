package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Usuario;
import br.com.logicup.backend.model.TipoUsuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> { // para herdar alguns métodos similares aos do sql (Usuario é a entidade e Integer a PK, no caso, Id)
    boolean existsByEmail(String email);

    // login: busca o usuário pelo e-mail (Optional = pode não existir)
    Optional<Usuario> findByEmail(String email);

    /** Sprint 10: conta já ligada a esta conta Google ("sub"). */
    Optional<Usuario> findByGoogleId(String googleId);

    // Sprint 7: username único (cadastro) e login por username
    boolean existsByUsername(String username);

    Optional<Usuario> findByUsername(String username);

    /**
     * Sprint 11: o filtro de conta bloqueada pergunta isto a cada requisição autenticada.
     * É uma consulta pela chave primária (rápida) que devolve só sim/não, sem carregar a entidade.
     */
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE u.id = :id AND u.bloqueadoEm IS NOT NULL")
    boolean estaBloqueado(@Param("id") Integer id);

    /**
     * Sprint 11 (admin): lista de usuários com busca por nome, nome de usuário ou e-mail (sem diferenciar
     * maiúsculas) e filtro opcional por perfil. Os parâmetros nulos significam "sem filtro".
     * Pageable: o banco devolve só uma página (LIMIT/OFFSET), e não a tabela inteira.
     */
    @Query("""
            SELECT u FROM Usuario u
             WHERE (:tipo IS NULL OR u.tipoUsuario = :tipo)
               AND (:busca IS NULL
                    OR LOWER(u.nome) LIKE :busca
                    OR LOWER(u.username) LIKE :busca
                    OR LOWER(u.email) LIKE :busca)
            """)
    Page<Usuario> buscarParaAdmin(@Param("busca") String busca, @Param("tipo") TipoUsuario tipo, Pageable pagina);

    /**
     * Sprint 10 (exclusão de conta): apaga a linha de usuario direto no banco (SQL nativo). O MySQL apaga o
     * resto sozinho, pelo ON DELETE CASCADE das chaves estrangeiras (aluno, progresso, conquistas, avatares,
     * matrículas, aceites e códigos de e-mail).
     *
     * Por que não usar o delete(usuario) do JpaRepository? O Hibernate guarda na memória (no "contexto de
     * persistência") as entidades já carregadas na transação, por exemplo o código do link que acabou de
     * ser usado (TokenUsuario), que aponta para o usuário. Com o delete(), na hora de gravar ele percebe que
     * esse código aponta para um usuário que não existe mais e recusa (TransientPropertyValueException).
     *
     * @Modifying: avisa o Spring que a consulta ALTERA dados (não é um SELECT).
     * flushAutomatically: antes do DELETE, grava o que estava pendente (ex.: "código usado").
     * clearAutomatically: depois do DELETE, esvazia a memória do Hibernate, para ninguém usar uma cópia velha.
     * @return quantas linhas de usuario foram apagadas (1, ou 0 se a conta já não existia)
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM usuario WHERE id_usuario = :id", nativeQuery = true)
    int apagarComTudoQueEhDele(@Param("id") Integer id);
}
