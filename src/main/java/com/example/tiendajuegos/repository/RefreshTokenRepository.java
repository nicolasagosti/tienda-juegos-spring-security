package com.example.tiendajuegos.repository;

import com.example.tiendajuegos.model.RefreshToken;
import com.example.tiendajuegos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken r set r.revocado = true where r.usuario = :usuario and r.revocado = false")
    void revocarTodosDe(Usuario usuario);
}
