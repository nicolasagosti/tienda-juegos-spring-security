package com.gamestore.auth.dto;

/** Cuerpo JSON unico de los errores ({@code {"mensaje": "..."}}), misma forma que negocio-service. */
public record ErrorResponseDto(String mensaje) {
}
