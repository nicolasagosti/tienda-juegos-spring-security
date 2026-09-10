package com.gamestore.negocio.usuarios.client;

/** auth-service (el unico servicio del que todavia dependemos por HTTP) no respondio. -&gt; HTTP 503. */
public class ServicioNoDisponibleException extends RuntimeException {

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
