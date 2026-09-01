package com.example.tiendajuegos.api.dto;

import com.example.tiendajuegos.model.Rol;
import com.example.tiendajuegos.model.Usuario;

/** Nunca exponemos la entidad Usuario tal cual por la API: asi el hash de la contraseña nunca sale del backend. */
public record UsuarioDTO(Long id, String username, String nombreCompleto, String email, Rol rol, boolean habilitado) {

    public static UsuarioDTO from(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getEmail(), u.getRol(), u.isHabilitado());
    }
}
