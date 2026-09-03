package com.gamestore.usuarios.repository;

import com.gamestore.usuarios.model.Rol;
import com.gamestore.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByRol(Rol rol);

    List<Usuario> findByIdIn(List<Long> ids);

    List<Usuario> findByUsernameIn(List<String> usernames);
}
