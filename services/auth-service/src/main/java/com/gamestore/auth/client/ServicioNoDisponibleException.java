package com.gamestore.auth.client;

/**
 * Un servicio del que dependemos (usuarios-service) no respondio a tiempo o
 * el circuit breaker esta abierto. Se traduce a HTTP 503 para el frontend
 * (ver ApiExceptionHandler), que puede reintentar mas tarde.
 */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
