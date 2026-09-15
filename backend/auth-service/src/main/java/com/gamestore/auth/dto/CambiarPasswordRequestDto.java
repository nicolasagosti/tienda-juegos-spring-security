package com.gamestore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo de {@code PUT /internal/credenciales/{username}/password}. */
public record CambiarPasswordRequestDto(

        @NotBlank
        @Size(min = 6, max = 100)
        String password) {
}
