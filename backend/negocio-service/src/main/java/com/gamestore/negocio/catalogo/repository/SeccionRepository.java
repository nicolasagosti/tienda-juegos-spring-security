package com.gamestore.negocio.catalogo.repository;

import com.gamestore.negocio.catalogo.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeccionRepository extends JpaRepository<Seccion, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    Optional<Seccion> findByNombreIgnoreCase(String nombre);
}
