package com.gamestore.catalogo.web;

import com.gamestore.catalogo.service.SeccionService;
import com.gamestore.catalogo.web.Dtos.CrearSeccionRequest;
import com.gamestore.catalogo.web.Dtos.SeccionDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secciones")
public class SeccionController {

    private final SeccionService seccionService;

    public SeccionController(SeccionService seccionService) {
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
