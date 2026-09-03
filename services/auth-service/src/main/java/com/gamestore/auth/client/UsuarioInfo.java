package com.gamestore.auth.client;

/** Lo que auth-service necesita saber del usuario y le pide a usuarios-service. */
public record UsuarioInfo(
        Long id,
        String username,
        String nombreCompleto,
        String email,
        String rol,
        boolean habilitado) {
}
