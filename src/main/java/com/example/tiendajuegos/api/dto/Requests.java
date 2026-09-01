package com.example.tiendajuegos.api.dto;

import com.example.tiendajuegos.model.Rol;

/** Cuerpos JSON de entrada de la API (todos records: inmutables, sin boilerplate). */
public class Requests {

    public record LoginRequest(String username, String password) {}

    /** Lo que devuelve /api/auth/login: el JWT que el frontend guarda y reenvia como "Authorization: Bearer ...". */
    public record LoginResponse(String token, UsuarioDTO usuario) {}

    public record CrearUsuarioRequest(String username, String password, String nombreCompleto, String email, Rol rol) {}

    public record ActualizarUsuarioRequest(String nombreCompleto, String email, Rol rol, boolean habilitado, String nuevaPassword) {}

    public record CrearSeccionRequest(String nombre, String descripcion) {}

    public record ErrorResponse(String mensaje) {}

    public record StatsDTO(long totalUsuarios, long totalAdmins, long totalVendedores,
                            long totalCompradores, long totalJuegos, long totalSecciones) {}
}
