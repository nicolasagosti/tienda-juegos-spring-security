package com.example.tiendajuegos.repository;

import com.example.tiendajuegos.model.Seccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeccionRepository extends JpaRepository<Seccion, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}
