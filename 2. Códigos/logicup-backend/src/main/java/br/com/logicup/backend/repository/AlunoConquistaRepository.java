package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.AlunoConquista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlunoConquistaRepository extends JpaRepository<AlunoConquista, AlunoConquista.Chave> {

    List<AlunoConquista> findByIdUsuario(Integer idUsuario);

    // M3: as que o aluno ainda não viu (para a comemoração "Nova conquista!"), na ordem em que foram ganhas
    List<AlunoConquista> findByIdUsuarioAndVistaEmIsNullOrderByDesbloqueadaEmAscCodigoConquistaAsc(Integer idUsuario);

    /*
     * Grava uma medalha SE ela ainda não existir.
     * INSERT IGNORE (MySQL): se a chave primária (aluno + conquista + nível) já existe, o banco ignora a linha
     * em vez de dar erro, e devolve 0 linhas inseridas. Assim o Service só credita as gemas quando o retorno é 1,
     * mesmo que duas requisições do mesmo aluno cheguem juntas.
     * O nível vai como texto ('BRONZE'...) porque a coluna no banco é ENUM.
     */
    @Modifying(flushAutomatically = true)
    @Query(value = "INSERT IGNORE INTO aluno_conquista (id_usuario, codigo_conquista, nivel, gemas_recebidas, desbloqueada_em) "
            + "VALUES (:alunoId, :codigo, :nivel, :gemas, :quando)", nativeQuery = true)
    int registrar(@Param("alunoId") Integer alunoId, @Param("codigo") String codigo, @Param("nivel") String nivel,
                  @Param("gemas") int gemas, @Param("quando") LocalDateTime quando);

    /*
     * Marca como vistas TODAS as que ainda não foram vistas.
     * clearAutomatically: depois do UPDATE em massa, limpa o cache do Hibernate desta transação,
     * para ninguém ler depois uma AlunoConquista "antiga" (com vistaEm ainda nulo) da memória.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE AlunoConquista c SET c.vistaEm = :agora WHERE c.idUsuario = :alunoId AND c.vistaEm IS NULL")
    int marcarVistas(@Param("alunoId") Integer alunoId, @Param("agora") LocalDateTime agora);
}
