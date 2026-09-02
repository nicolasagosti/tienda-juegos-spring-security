package com.gamestore.usuarios.client;

/** Un servicio del que dependemos (auth-service o catalogo-service) no respondio. -> HTTP 503. */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
