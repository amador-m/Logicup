package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.AceiteDocumento;
import br.com.logicup.backend.model.DocumentoLegal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AceiteDocumentoRepository extends JpaRepository<AceiteDocumento, Integer> {

    /** O usuário já aceitou ESTA versão deste documento? (evita linha repetida) */
    boolean existsByUsuarioIdAndDocumentoAndVersao(Integer usuarioId, DocumentoLegal documento, String versao);
}
