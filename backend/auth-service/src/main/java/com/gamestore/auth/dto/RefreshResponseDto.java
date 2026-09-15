package com.gamestore.auth.dto;

/** Respuesta del refresh: access token nuevo y refresh token rotado. */
public record RefreshResponseDto(String token, String refreshToken) {
}
