package com.gamestore.usuarios.service;

/** Operacion valida pero imposible por el estado actual (ej: borrar un usuario con juegos). -> HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String mensaje) {
        super(mensaje);
    }
}
