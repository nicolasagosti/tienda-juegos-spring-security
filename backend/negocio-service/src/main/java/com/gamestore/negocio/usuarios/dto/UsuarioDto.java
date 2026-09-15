package com.gamestore.negocio.usuarios.dto;

import com.gamestore.negocio.usuarios.model.Rol;
import com.gamestore.negocio.usuarios.model.Usuario;

/** Respuesta JSON de un usuario. Misma forma que el UsuarioDTO del monolito / usuarios-service. */
public record UsuarioDto(Long id, String username, String nombreCompleto, String email, Rol rol, boolean habilitado) {

    public static UsuarioDto from(Usuario u) {
        return new UsuarioDto(u.getId(), u.getUsername(), u.getNombreCompleto(),
                u.getEmail(), u.getRol(), u.isHabilitado());
    }
}
