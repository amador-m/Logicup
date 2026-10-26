package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.AlunoAvatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlunoAvatarRepository extends JpaRepository<AlunoAvatar, AlunoAvatar.Chave> {

    List<AlunoAvatar> findByIdUsuario(Integer idUsuario);

    boolean existsByIdUsuarioAndCodigoAvatar(Integer idUsuario, String codigoAvatar);

    /*
     * Grava a compra com um INSERT de verdade (e não save()).
     * Por quê? A chave é preenchida por nós (aluno + avatar). Nesse caso o save() do Spring Data acha que a
     * linha "pode já existir", faz um SELECT e, se outra requisição tiver acabado de gravar a mesma compra,
     * faria um UPDATE silencioso. Com o INSERT, a chave primária RECUSA a duplicata: o Spring traduz o erro
     * para DataIntegrityViolationException (409) e a transação inteira, inclusive o débito das gemas, é desfeita.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT INTO aluno_avatar (id_usuario, codigo_avatar, preco_pago, comprado_em) "
            + "VALUES (:alunoId, :codigo, :preco, :quando)", nativeQuery = true)
    int registrarCompra(@Param("alunoId") Integer alunoId, @Param("codigo") String codigo,
                        @Param("preco") int preco, @Param("quando") LocalDateTime quando);
}
