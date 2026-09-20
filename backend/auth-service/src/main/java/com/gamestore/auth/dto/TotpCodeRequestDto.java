package com.gamestore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Cuerpo de {@code POST /api/auth/2fa/enable} y {@code /disable}: un codigo TOTP de 6 digitos. */
public record TotpCodeRequestDto(

        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "debe ser un codigo numerico de 6 digitos")
        String codigo) {
}
