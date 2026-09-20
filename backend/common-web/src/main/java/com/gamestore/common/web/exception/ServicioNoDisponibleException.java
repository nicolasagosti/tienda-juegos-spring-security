package com.gamestore.common.web.exception;

/**
 * Un servicio del que dependemos por HTTP no respondio a tiempo, o el circuit
 * breaker esta abierto. La traduce {@link GlobalExceptionHandler} a HTTP 503,
 * que el frontend puede reintentar mas tarde.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
