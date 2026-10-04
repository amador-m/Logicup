package br.com.logicup.backend.repository;

import br.com.logicup.backend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> { // para herdar alguns métodos similares aos do sql (Usuario é a entidade e Integer a PK, no caso, Id)
    boolean existsByEmail(String email);

    // login: busca o usuário pelo e-mail (Optional = pode não existir)
    Optional<Usuario> findByEmail(String email);
}