package com.gamestore.usuarios.web;

import com.gamestore.usuarios.client.CatalogoClient;
import com.gamestore.usuarios.model.Rol;
import com.gamestore.usuarios.service.UsuarioService;
import com.gamestore.usuarios.web.Dtos.StatsDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard del ADMIN. Los totales de usuarios salen de la base local; los
 * de juegos/secciones los trae catalogo-service (si esta caido, vienen en
 * -1 y el front lo muestra como "n/d", sin romper la pantalla).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final UsuarioService usuarioService;
    private final CatalogoClient catalogoClient;

    public AdminStatsController(UsuarioService usuarioService, CatalogoClient catalogoClient) {
        this.usuarioService = usuarioService;
        this.catalogoClient = catalogoClient;
    }

    @GetMapping("/stats")
    public StatsDTO stats() {
        long admins = usuarioService.contarPorRol(Rol.ADMIN);
        long vendedores = usuarioService.contarPorRol(Rol.VENDEDOR);
        long compradores = usuarioService.contarPorRol(Rol.COMPRADOR);
        CatalogoClient.CatalogoStats cat = catalogoClient.stats();
        return new StatsDTO(admins + vendedores + compradores, admins, vendedores, compradores,
                cat.totalJuegos(), cat.totalSecciones());
    }
}
