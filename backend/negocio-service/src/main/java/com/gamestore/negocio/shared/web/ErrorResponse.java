package com.gamestore.negocio.shared.web;

/**
 * Cuerpo JSON unico para los errores de toda la API ({@code {"mensaje": "..."}}).
 * Misma forma que usaban catalogo-service y usuarios-service por separado, asi
 * el frontend no cambia.
 */
public record ErrorResponse(String mensaje) {
}
