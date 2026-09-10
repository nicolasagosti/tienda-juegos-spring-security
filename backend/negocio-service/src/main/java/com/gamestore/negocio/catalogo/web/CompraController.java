package com.gamestore.negocio.catalogo.web;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.negocio.catalogo.service.CompraService;
import com.gamestore.negocio.catalogo.web.dto.CompraDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Compra ficticia de juegos. Solo el rol COMPRADOR puede comprar; cada quien
 * ve unicamente sus propias compras (se filtra por el username del JWT, no
 * por un parametro que el cliente pueda cambiar).
 */
@RestController
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping("/api/juegos/{id}/comprar")
    @PreAuthorize("hasRole('COMPRADOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public CompraDto comprar(@PathVariable Long id, @AuthenticationPrincipal AuthPrincipal principal) {
        return CompraDto.from(compraService.comprar(id, principal));
    }

    @GetMapping("/api/compras")
    @PreAuthorize("hasRole('COMPRADOR')")
    public List<CompraDto> misCompras(@AuthenticationPrincipal AuthPrincipal principal) {
        return compraService.misCompras(principal.username()).stream()
                .map(CompraDto::from)
                .toList();
    }
}
