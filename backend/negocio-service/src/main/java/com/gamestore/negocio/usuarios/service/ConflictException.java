package com.gamestore.negocio.usuarios.service;

/** Operacion valida pero imposible por el estado actual (ej: borrar un usuario con juegos). -&gt; HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensaje) {
        super(mensaje);
    }
}
