package com.gamestore.catalogo.repository;

import com.gamestore.catalogo.model.Juego;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JuegoRepository extends JpaRepository<Juego, Long> {

    List<Juego> findByVendedorUsername(String vendedorUsername);

    long countByVendedorUsername(String vendedorUsername);

    long countBySeccionId(Long seccionId);
}
