package com.gamestore.common.web.exception;

/**
 * Operacion valida pero imposible por el estado actual del sistema
 * (ej: borrar un usuario que todavia tiene juegos publicados).
 * La traduce {@link GlobalExceptionHandler} a HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensaje) {
        super(mensaje);
    }
}
