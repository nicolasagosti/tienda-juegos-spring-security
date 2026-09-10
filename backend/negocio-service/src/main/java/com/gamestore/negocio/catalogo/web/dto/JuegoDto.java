package com.gamestore.negocio.catalogo.web.dto;

import java.math.BigDecimal;

/**
 * Respuesta JSON de un juego. Misma forma que en el monolito / catalogo-service:
 * el vendedor viene embebido y {@code puedeEditar} / {@code comprado} los
 * calcula el servidor con el username y el rol del JWT.
 */
public record JuegoDto(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        int stock,
        String imagenUrl,
        SeccionDto seccion,
        VendedorDto vendedor,
        boolean puedeEditar,
        boolean comprado) {
}
