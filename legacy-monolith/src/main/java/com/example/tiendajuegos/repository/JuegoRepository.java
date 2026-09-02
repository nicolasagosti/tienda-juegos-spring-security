package com.example.tiendajuegos.repository;

import com.example.tiendajuegos.model.Juego;
import com.example.tiendajuegos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JuegoRepository extends JpaRepository<Juego, Long> {

    List<Juego> findByVendedor(Usuario vendedor);

    List<Juego> findBySeccionId(Long seccionId);

    long countByVendedor(Usuario vendedor);
}
