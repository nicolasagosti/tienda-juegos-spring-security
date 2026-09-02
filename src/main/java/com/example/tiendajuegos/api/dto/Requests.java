package com.example.tiendajuegos.api.dto;

import com.example.tiendajuegos.model.Rol;

/** Cuerpos JSON de entrada/salida de la API (todos records: inmutables, sin boilerplate). */
public class Requests {

    /** totpCode es opcional: solo hace falta si el usuario tiene 2FA activado (ver AuthApiController.login). */
    public record LoginRequest(String username, String password, String totpCode) {}

    /** Lo que devuelve /api/auth/login: access token corto + refresh token largo (ver JwtService / RefreshTokenService). */
    public record LoginResponse(String token, String refreshToken, UsuarioDTO usuario) {}

    public record RefreshRequest(String refreshToken) {}

    public record RefreshResponse(String token, String refreshToken) {}

    public record CrearUsuarioRequest(String username, String password, String nombreCompleto, String email, Rol rol) {}

    public record ActualizarUsuarioRequest(String nombreCompleto, String email, Rol rol, boolean habilitado, String nuevaPassword) {}

    public record CrearSeccionRequest(String nombre, String descripcion) {}

    public record ErrorResponse(String mensaje) {}

    public record MensajeResponse(String mensaje) {}

    /** requiere2fa=true le dice al frontend "la contraseña esta bien, mostra el segundo paso y reenvia con totpCode". */
    public record LoginErrorResponse(String mensaje, boolean requiere2fa) {}

    public record TotpSetupResponse(String secret, String otpauthUri) {}

    public record TotpCodeRequest(String codigo) {}

    public record StatsDTO(long totalUsuarios, long totalAdmins, long totalVendedores,
                            long totalCompradores, long totalJuegos, long totalSecciones) {}
}
