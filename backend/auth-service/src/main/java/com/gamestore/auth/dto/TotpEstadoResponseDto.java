package com.gamestore.auth.dto;

/** Respuesta de {@code GET /api/auth/2fa/estado}. */
public record TotpEstadoResponseDto(boolean habilitado) {
}
