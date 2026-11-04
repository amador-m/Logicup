package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.FinalidadeToken;
import br.com.logicup.backend.model.TokenUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Sprint 10: códigos dos links enviados por e-mail. */
public interface TokenUsuarioRepository extends JpaRepository<TokenUsuario, Integer> {

    /** Busca pelo HASH (é só o hash que fica no banco). O JOIN FETCH já traz o usuário junto. */
    @Query("SELECT t FROM TokenUsuario t JOIN FETCH t.usuario WHERE t.hashToken = :hash")
    Optional<TokenUsuario> buscarPorHash(@Param("hash") String hash);

    /**
     * Os códigos ainda não usados de uma finalidade (normalmente 0 ou 1; no máximo uns poucos, pelo limite
     * de pedidos). O Service "aposenta" cada um pela própria entidade (TokenUsuario.aposentar).
     *
     * Por que não um UPDATE em lote (@Modifying)? O UPDATE em lote vai direto ao banco e NÃO atualiza as
     * entidades que o Hibernate já tem em memória nesta transação: um código antigo já carregado
     * continuaria parecendo válido. Limpar a memória (clearAutomatically) resolveria isso, mas soltaria
     * também o Usuario que o AuthService ainda está alterando, e a confirmação do e-mail não seria gravada
     * (bug pego pelos testes). Mexer nas entidades evita os dois problemas.
     */
    @Query("SELECT t FROM TokenUsuario t WHERE t.usuario.id = :usuarioId AND t.finalidade = :finalidade "
            + "AND t.usadoEm IS NULL")
    List<TokenUsuario> buscarPendentes(@Param("usuarioId") Integer usuarioId,
                                       @Param("finalidade") FinalidadeToken finalidade);

    /**
     * Sprint 10 (limpeza): apaga os códigos que venceram antes da data-limite (usados ou não).
     * Aqui o DELETE em lote é a escolha certa: roda sozinho, numa transação própria (LimpezaDeTokens),
     * sem nenhuma entidade carregada em memória que pudesse ficar desatualizada.
     * @return quantas linhas foram apagadas
     */
    @Modifying
    @Query("DELETE FROM TokenUsuario t WHERE t.expiraEm < :limite")
    int apagarVencidosAntesDe(@Param("limite") LocalDateTime limite);
}
