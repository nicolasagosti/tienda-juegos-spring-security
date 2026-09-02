package com.gamestore.auth.web;

/** Cuerpos JSON de entrada/salida de auth-service (mismos nombres que el monolito). */
public class Dtos {

    public record LoginRequest(String username, String password, String totpCode) {}

    public record LoginResponse(String token, String refreshToken, UsuarioDTO usuario) {}

    public record RefreshRequest(String refreshToken) {}

    public record RefreshResponse(String token, String refreshToken) {}

    public record ErrorResponse(String mensaje) {}

    public record MensajeResponse(String mensaje) {}

    /** requiere2fa=true -> el frontend muestra el segundo paso y reenvia con totpCode. */
    public record LoginErrorResponse(String mensaje, boolean requiere2fa) {}

    public record TotpSetupResponse(String secret, String otpauthUri) {}

    public record TotpCodeRequest(String codigo) {}

    /** Igual que el UsuarioDTO del monolito; se arma con datos que trae usuarios-service. */
    public record UsuarioDTO(Long id, String username, String nombreCompleto, String email, String rol, boolean habilitado) {}
}
