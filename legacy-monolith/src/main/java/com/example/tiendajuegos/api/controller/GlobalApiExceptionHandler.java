package com.example.tiendajuegos.api.controller;

import com.example.tiendajuegos.api.dto.Requests.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Traduce las excepciones de negocio/seguridad a respuestas JSON prolijas
 * para el frontend, en vez del stacktrace/pagina de error por defecto.
 *
 * Ojo con el "basePackages": lo restringimos a los controllers de la API
 * a proposito. Los controllers viejos (Thymeleaf, en el paquete
 * "controller") NO pasan por aca: ellos siguen usando accessDeniedPage()
 * y las paginas de error HTML configuradas en SecurityConfig. Asi las dos
 * interfaces (REST para React, MVC clasica con vistas) conviven sin
 * pisarse el manejo de errores.
 */
@RestControllerAdvice(basePackages = "com.example.tiendajuegos.api.controller")
public class GlobalApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("No tenes permiso para realizar esta accion"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("No se puede completar: hay datos relacionados que lo impiden"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse("La imagen es demasiado grande (maximo 5MB)"));
    }
}
