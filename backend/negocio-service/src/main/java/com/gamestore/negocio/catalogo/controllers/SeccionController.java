package com.gamestore.negocio.catalogo.controllers;

import com.gamestore.negocio.catalogo.service.SeccionService;
import com.gamestore.negocio.catalogo.dto.CrearSeccionRequestDto;
import com.gamestore.negocio.catalogo.dto.SeccionDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/secciones")
public class SeccionController {

    private final SeccionService seccionService;

    public SeccionController(SeccionService seccionService) {
        this.seccionService = seccionService;
    }

    @GetMapping
    public List<SeccionDto> listar() {
        return seccionService.listarTodas().stream().map(SeccionDto::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SeccionDto crear(@Valid @RequestBody CrearSeccionRequestDto req) {
        return SeccionDto.from(seccionService.crear(req.nombre(), req.descripcion()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        seccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
