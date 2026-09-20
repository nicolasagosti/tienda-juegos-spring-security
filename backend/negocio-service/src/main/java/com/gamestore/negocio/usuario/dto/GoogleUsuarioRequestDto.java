package com.gamestore.negocio.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de {@code POST /internal/usuarios/google} (lo manda auth-service
 * despues de un login con Google OK). El email es la clave con la que se
 * busca o se crea el perfil; el nombre es opcional (si no viene, el servicio
 * usa el username generado).
 */
public record GoogleUsuarioRequestDto(

        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Size(max = 255)
        String nombre) {
}
