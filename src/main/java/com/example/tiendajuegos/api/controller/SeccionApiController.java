package com.example.tiendajuegos.api.controller;

import com.example.tiendajuegos.api.dto.Requests.CrearSeccionRequest;
import com.example.tiendajuegos.api.dto.SeccionDTO;
import com.example.tiendajuegos.service.SeccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secciones")
public class SeccionApiController {

    private final SeccionService seccionService;

    public SeccionApiController(SeccionService seccionService) {
        this.seccionService = seccionService;
    }

    @GetMapping
    public List<SeccionDTO> listar() {
        return seccionService.listarTodas().stream().map(SeccionDTO::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SeccionDTO crear(@RequestBody CrearSeccionRequest req) {
        return SeccionDTO.from(seccionService.crear(req.nombre(), req.descripcion()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        seccionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
