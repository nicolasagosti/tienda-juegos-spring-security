package com.gamestore.negocio.usuario.controller;

import com.gamestore.negocio.usuario.model.Rol;
import com.gamestore.negocio.usuario.service.UsuarioService;
import com.gamestore.negocio.usuario.spi.ConsultaCatalogo;
import com.gamestore.negocio.usuario.dto.StatsDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard del ADMIN. Los totales de usuarios salen de la base local; los
 * de juegos/secciones los da el sub-dominio catalogo en memoria (ya no puede
 * "estar caido", asi que se fue el degradado a -1).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatsController {

    private final UsuarioService usuarioService;
    private final ConsultaCatalogo catalogo;

    public AdminStatsController(UsuarioService usuarioService, ConsultaCatalogo catalogo) {
        this.usuarioService = usuarioService;
        this.catalogo = catalogo;
    }

    @GetMapping("/stats")
    public StatsDto stats() {
        long admins = usuarioService.contarPorRol(Rol.ADMIN);
        long vendedores = usuarioService.contarPorRol(Rol.VENDEDOR);
        long compradores = usuarioService.contarPorRol(Rol.COMPRADOR);
        ConsultaCatalogo.Totales cat = catalogo.totales();
        return new StatsDto(admins + vendedores + compradores, admins, vendedores, compradores,
                cat.totalJuegos(), cat.totalSecciones());
    }
}
