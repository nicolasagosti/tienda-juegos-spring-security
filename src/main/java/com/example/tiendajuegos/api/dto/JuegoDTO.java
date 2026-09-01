package com.example.tiendajuegos.api.dto;

import com.example.tiendajuegos.model.Juego;
import com.example.tiendajuegos.model.Rol;
import com.example.tiendajuegos.model.Usuario;

import java.math.BigDecimal;

public record JuegoDTO(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String imagenUrl,
        SeccionDTO seccion,
        UsuarioDTO vendedor,
        boolean puedeEditar
) {

    /**
     * "puedeEditar" se calcula del lado del servidor (no confiamos en el
     * frontend para decidir esto): le dice a React si tiene que mostrar los
     * botones Editar/Eliminar para ESTE usuario logueado. Aunque el
     * frontend los oculte, el backend vuelve a validar todo en cada
     * PUT/DELETE real.
     */
    public static JuegoDTO from(Juego j, Usuario usuarioActual) {
        boolean puedeEditar = usuarioActual.getRol() == Rol.ADMIN
                || (usuarioActual.getRol() == Rol.VENDEDOR && j.getVendedor().getId().equals(usuarioActual.getId()));
        return new JuegoDTO(
                j.getId(),
                j.getNombre(),
                j.getDescripcion(),
                j.getPrecio(),
                j.getImagenUrl(),
                SeccionDTO.from(j.getSeccion()),
                UsuarioDTO.from(j.getVendedor()),
                puedeEditar
        );
    }
}
