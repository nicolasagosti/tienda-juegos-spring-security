package com.gamestore.negocio.catalogo.dto;

import com.gamestore.negocio.catalogo.model.Seccion;

/** Respuesta JSON de una seccion. */
public record SeccionDto(Long id, String nombre, String descripcion) {

    public static SeccionDto from(Seccion s) {
        return s == null ? null : new SeccionDto(s.getId(), s.getNombre(), s.getDescripcion());
    }
}
