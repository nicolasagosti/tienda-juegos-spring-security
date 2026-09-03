package com.gamestore.auth.web;

import com.gamestore.auth.client.ServicioNoDisponibleException;
import com.gamestore.auth.service.AuthExceptions;
import com.gamestore.auth.web.Dtos.ErrorResponse;
import com.gamestore.auth.web.Dtos.LoginErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce las excepciones de negocio a JSON, como el GlobalApiExceptionHandler del monolito. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthExceptions.DosFactoresRequerido.class)
    public ResponseEntity<LoginErrorResponse> handle2fa(AuthExceptions.DosFactoresRequerido e) {
        return ResponseEntity.status(401).body(new LoginErrorResponse(e.getMessage(), true));
    }

    @ExceptionHandler({AuthExceptions.NoAutorizado.class, AuthExceptions.CuentaDeshabilitada.class})
    public ResponseEntity<ErrorResponse> handleNoAutorizado(RuntimeException e) {
        return ResponseEntity.status(401).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleDown(ServicioNoDisponibleException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Servicio no disponible momentaneamente, proba de nuevo en unos segundos"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }
}
