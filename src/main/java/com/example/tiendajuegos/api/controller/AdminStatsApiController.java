package com.example.tiendajuegos.api.controller;

import com.example.tiendajuegos.api.dto.Requests.StatsDTO;
import com.example.tiendajuegos.model.Rol;
import com.example.tiendajuegos.service.JuegoService;
import com.example.tiendajuegos.service.SeccionService;
import com.example.tiendajuegos.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsApiController {

    private final UsuarioService usuarioService;
    private final SeccionService seccionService;
    private final JuegoService juegoService;

    public AdminStatsApiController(UsuarioService usuarioService, SeccionService seccionService, JuegoService juegoService) {
        this.usuarioService = usuarioService;
        this.seccionService = seccionService;
        this.juegoService = juegoService;
    }

    @GetMapping("/stats")
    public StatsDTO stats() {
        return new StatsDTO(
                usuarioService.listarTodos().size(),
                usuarioService.contarPorRol(Rol.ADMIN),
                usuarioService.contarPorRol(Rol.VENDEDOR),
                usuarioService.contarPorRol(Rol.COMPRADOR),
                juegoService.listarTodos().size(),
                seccionService.listarTodas().size()
        );
    }
}
