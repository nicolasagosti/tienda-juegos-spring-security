package com.gamestore.negocio.catalogo.dto;

import com.gamestore.negocio.catalogo.model.Compra;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Una compra ficticia, tal como la ve el comprador en "Mis compras". */
public record CompraDto(
        Long id,
        Long juegoId,
        String nombreJuego,
        String imagenUrl,
        BigDecimal precio,
        LocalDateTime fechaCompra) {

    public static CompraDto from(Compra c) {
        return new CompraDto(c.getId(), c.getJuegoId(), c.getNombreJuego(),
                c.getImagenUrl(), c.getPrecio(), c.getFechaCompra());
    }
}
