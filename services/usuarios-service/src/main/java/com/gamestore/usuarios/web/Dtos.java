package com.gamestore.usuarios.web;

import com.gamestore.usuarios.model.Rol;
import com.gamestore.usuarios.model.Usuario;

/** Cuerpos JSON de usuarios-service. UsuarioDTO tiene la misma forma que en el monolito. */
public class Dtos {

    public record UsuarioDTO(Long id, String username, String nombreCompleto, String email, Rol rol, boolean habilitado) {
        public static UsuarioDTO from(Usuario u) {
            return new UsuarioDTO(u.getId(), u.getUsername(), u.getNombreCompleto(), u.getEmail(), u.getRol(), u.isHabilitado());
        }
    }

    public record CrearUsuarioRequest(String username, String password, String nombreCompleto, String email, Rol rol) {}

    public record ActualizarUsuarioRequest(String nombreCompleto, String email, Rol rol, boolean habilitado, String nuevaPassword) {}

    public record StatsDTO(long totalUsuarios, long totalAdmins, long totalVendedores,
                           long totalCompradores, long totalJuegos, long totalSecciones) {}

    public record ErrorResponse(String mensaje) {}
}
