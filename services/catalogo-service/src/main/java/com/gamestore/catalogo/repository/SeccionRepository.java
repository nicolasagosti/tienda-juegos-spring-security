package com.gamestore.catalogo.repository;

import com.gamestore.catalogo.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeccionRepository extends JpaRepository<Seccion, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}
