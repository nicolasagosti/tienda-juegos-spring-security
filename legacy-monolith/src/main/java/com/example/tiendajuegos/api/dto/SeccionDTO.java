package com.example.tiendajuegos.api.dto;

import com.example.tiendajuegos.model.Seccion;

public record SeccionDTO(Long id, String nombre, String descripcion) {

    public static SeccionDTO from(Seccion s) {
        if (s == null) {
            return null;
        }
        return new SeccionDTO(s.getId(), s.getNombre(), s.getDescripcion());
    }
}
