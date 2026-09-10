package com.gamestore.negocio.catalogo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo del alta de seccion ({@code POST /api/secciones}). */
public record CrearSeccionRequest(

        @NotBlank
        @Size(max = 60)
        String nombre,

        @Size(max = 255)
        String descripcion) {
}
