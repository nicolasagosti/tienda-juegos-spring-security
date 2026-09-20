package com.gamestore.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /internal/credenciales} (lo manda negocio-service
 * cuando el ADMIN da de alta un usuario). Mismos limites que el
 * {@code CrearUsuarioRequestDto} de negocio-service, asi los dos lados
 * rechazan lo mismo.
 *
 * @param email puede venir vacio (negocio-service manda "" cuando el
 *              usuario no tiene email); si trae texto, tiene que ser un email.
 */
public record CrearCredencialRequestDto(

        @NotBlank
        @Size(min = 3, max = 50)
        String username,

        @Email
        @Size(max = 255)
        String email,

        @NotBlank
        @Size(min = 6, max = 100)
        String password) {
}
