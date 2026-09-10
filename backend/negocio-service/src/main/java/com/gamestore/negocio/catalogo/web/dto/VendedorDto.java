package com.gamestore.negocio.catalogo.web.dto;

import com.gamestore.negocio.catalogo.spi.ResolucionVendedores;

/**
 * El "vendedor" embebido en cada juego, tal como lo ve el frontend. Se arma a
 * partir de {@link ResolucionVendedores.Vendedor} (el DTO del puerto).
 */
public record VendedorDto(Long id, String username, String nombreCompleto, String email,
                          String rol, boolean habilitado) {

    public static VendedorDto from(ResolucionVendedores.Vendedor v) {
        return new VendedorDto(v.id(), v.username(), v.nombreCompleto(), v.email(), v.rol(), v.habilitado());
    }
}
