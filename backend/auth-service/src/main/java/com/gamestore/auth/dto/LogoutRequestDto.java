package com.gamestore.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /api/auth/logout}. A diferencia de
 * {@link RefreshRequestDto}, el token es opcional: sin el, el logout es un
 * no-op del lado del servidor (el frontend igual descarta el access token).
 */
public record LogoutRequestDto(

        @Size(max = 100)
        String refreshToken) {
}
