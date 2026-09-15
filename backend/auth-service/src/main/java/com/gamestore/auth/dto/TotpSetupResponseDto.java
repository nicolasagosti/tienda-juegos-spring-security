package com.gamestore.auth.dto;

/** Respuesta de {@code POST /api/auth/2fa/setup}: secreto Base32 + URI para el QR. */
public record TotpSetupResponseDto(String secret, String otpauthUri) {
}
