package com.gamestore.auth.service;

/** Excepciones de negocio del login, que ApiExceptionHandler traduce a JSON + status. */
public class AuthExceptions {

    private AuthExceptions() {
    }

    /** Contraseña OK pero la cuenta tiene 2FA y falta (o esta mal) el codigo. -> 401 con requiere2fa=true. */
    public static class DosFactoresRequerido extends RuntimeException {
        public DosFactoresRequerido(String mensaje) {
            super(mensaje);
        }
    }

    /** La cuenta esta deshabilitada por el ADMIN (dato que vive en usuarios-service). -> 401. */
    public static class CuentaDeshabilitada extends RuntimeException {
        public CuentaDeshabilitada(String mensaje) {
            super(mensaje);
        }
    }

    /** Credenciales incorrectas, cuenta bloqueada, sesion expirada, etc. -> 401. */
    public static class NoAutorizado extends RuntimeException {
        public NoAutorizado(String mensaje) {
            super(mensaje);
        }
    }
}
