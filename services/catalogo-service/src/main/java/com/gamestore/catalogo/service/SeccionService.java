package com.gamestore.catalogo.service;

import com.gamestore.catalogo.model.Seccion;
import com.gamestore.catalogo.repository.SeccionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/** Alta/baja de secciones. Exclusivo del ADMIN. Sin cambios respecto del monolito. */
@Service
public class SeccionService {

    private final SeccionRepository seccionRepository;

    public SeccionService(SeccionRepository seccionRepository) {
        this.seccionRepository = seccionRepository;
    }

    public List<Seccion> listarTodas() {
        return seccionRepository.findAll();
    }

    public Seccion crear(String nombre, String descripcion) {
        if (seccionRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe una seccion con ese nombre");
        }
        return seccionRepository.save(new Seccion(nombre, descripcion));
    }

    public void eliminar(Long id) {
        seccionRepository.deleteById(id);
    }
}
