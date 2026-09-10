package com.gamestore.negocio.catalogo.repository;

import com.gamestore.negocio.catalogo.model.Juego;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JuegoRepository extends JpaRepository<Juego, Long> {

    List<Juego> findByVendedorUsername(String vendedorUsername);

    long countByVendedorUsername(String vendedorUsername);

    long countBySeccionId(Long seccionId);

    Optional<Juego> findByNombreIgnoreCase(String nombre);

    /**
     * Igual que findById pero tomando un lock de escritura sobre la fila:
     * dos compras simultaneas del mismo juego se serializan y no pueden
     * dejar el stock negativo (sobreventa).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from Juego j where j.id = :id")
    Optional<Juego> findByIdParaActualizar(@Param("id") Long id);
}
