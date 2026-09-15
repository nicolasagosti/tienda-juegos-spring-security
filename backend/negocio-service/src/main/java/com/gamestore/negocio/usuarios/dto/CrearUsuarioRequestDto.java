package com.gamestore.negocio.usuarios.dto;

import com.gamestore.negocio.usuarios.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del alta de usuario ({@code POST /api/usuarios}). Se valida con
 * {@code @Valid} en el controller antes de tocar el servicio.
 *
 * @param email puede venir null/vacio (el monolito lo permitia); si viene,
 *              tiene que tener forma de email.
 */
public record CrearUsuarioRequestDto(

        @NotBlank
        @Size(min = 3, max = 50)
        String username,

        @NotBlank
        @Size(min = 6, max = 100)
        String password,

        @NotBlank
        @Size(max = 255)
        String nombreCompleto,

        @Email
        @Size(max = 255)
        String email,

        @NotNull
        Rol rol) {
}
