package com.gamestore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /api/auth/login}.
 *
 * @param totpCode opcional: el frontend lo manda recien en el segundo paso
 *                 (cuando el primer intento respondio {@code requiere2fa}).
 *                 Si viene, tiene que ser numerico; que sea el codigo
 *                 correcto lo decide {@code AuthService}, no la validacion.
 */
public record LoginRequestDto(

        @NotBlank
        @Size(max = 50)
        String username,

        @NotBlank
        @Size(max = 100)
        String password,

        @Pattern(regexp = "\\d{0,6}", message = "debe ser un codigo numerico de 6 digitos")
        String totpCode) {
}
