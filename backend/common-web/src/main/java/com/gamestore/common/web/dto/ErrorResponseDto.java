package com.gamestore.common.web.dto;

/**
 * Cuerpo JSON unico de TODOS los errores de la API ({@code {"mensaje": "..."}}).
 *
 * Vive aca y no en cada servicio para que auth-service y negocio-service no
 * puedan divergir: el frontend parsea un solo contrato de error.
 */
public record ErrorResponseDto(String mensaje) {
}
