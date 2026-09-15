package com.gamestore.negocio.usuarios.dto;

import com.gamestore.negocio.usuarios.model.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de la edicion de usuario ({@code PUT /api/usuarios/{id}}).
 *
 * @param nuevaPassword opcional: null o vacio = "no cambiar la contrasena".
 *                      Si viene con texto, tiene que cumplir el mismo minimo
 *                      que el alta (6 caracteres; auth-service exige lo mismo)
 *                      y el servicio la delega a auth-service.
 */
public record ActualizarUsuarioRequestDto(

        @NotBlank
        @Size(max = 255)
        String nombreCompleto,

        @Email
        @Size(max = 255)
        String email,

        @NotNull
        Rol rol,

        boolean habilitado,

        @Size(max = 100)
        @Pattern(regexp = "(?s)^\\s*$|^.{6,}$", message = "debe tener al menos 6 caracteres")
        String nuevaPassword) {
}
