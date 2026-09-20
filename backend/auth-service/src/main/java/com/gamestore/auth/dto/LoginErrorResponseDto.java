package com.gamestore.auth.dto;

/** Error de login. {@code requiere2fa=true} -> el frontend muestra el segundo paso y reenvia con totpCode. */
public record LoginErrorResponseDto(String mensaje, boolean requiere2fa) {
}
