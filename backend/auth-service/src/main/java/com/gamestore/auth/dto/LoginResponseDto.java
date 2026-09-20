package com.gamestore.auth.dto;

/** Respuesta del login OK: par de tokens + el perfil que trae negocio-service. */
public record LoginResponseDto(String token, String refreshToken, UsuarioDto usuario) {
}
