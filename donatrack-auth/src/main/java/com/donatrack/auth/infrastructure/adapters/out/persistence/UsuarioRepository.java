package com.donatrack.auth.infrastructure.adapters.out.persistence;

import com.donatrack.auth.domain.entities.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
  Optional<Usuario> findByUsername(String username);
  Optional<Usuario> findByEmail(String email);
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
}

