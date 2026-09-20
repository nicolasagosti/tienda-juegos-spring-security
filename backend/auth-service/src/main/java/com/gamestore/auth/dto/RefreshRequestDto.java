package com.gamestore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code POST /api/auth/refresh}: el refresh token es obligatorio. */
public record RefreshRequestDto(

        @NotBlank
        @Size(max = 100)
        String refreshToken) {
}
