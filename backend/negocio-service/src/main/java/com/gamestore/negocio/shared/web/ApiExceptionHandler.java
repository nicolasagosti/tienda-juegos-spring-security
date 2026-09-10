package com.gamestore.negocio.shared.web;

import com.gamestore.negocio.usuarios.client.ServicioNoDisponibleException;
import com.gamestore.negocio.usuarios.service.ConflictException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Traduce las excepciones de las dos mitades del dominio a la misma forma
 * JSON ({@link ErrorResponse}). Es la union de los dos {@code ApiExceptionHandler}
 * que tenian catalogo-service y usuarios-service, mas los handlers de
 * Bean Validation (400 con el primer mensaje util en vez del stacktrace de
 * Spring).
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    /** Compensacion fallida en un alta de usuario (auth-service no respondio). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException e) {
        String mensaje = e instanceof ConflictException
                ? e.getMessage()
                : "No se puede completar: hay datos relacionados que lo impiden";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(mensaje));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("No tenes permiso para realizar esta accion"));
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleDown(ServicioNoDisponibleException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("Servicio no disponible momentaneamente, proba de nuevo en unos segundos"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse("La imagen es demasiado grande (maximo 5MB)"));
    }

    // ---------- Bean Validation ----------

    /** Falla el @Valid de un @RequestBody (records de request de usuarios/secciones). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyInvalido(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(ApiExceptionHandler::describir)
                .orElse("Datos invalidos");
        return ResponseEntity.badRequest().body(new ErrorResponse(mensaje));
    }

    /** Falla una constraint sobre parametros de metodo o sobre un @ModelAttribute (form de juegos). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodInvalido(HandlerMethodValidationException e) {
        String mensaje = e.getAllErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage() == null ? "Datos invalidos" : err.getDefaultMessage())
                .orElse("Datos invalidos");
        return ResponseEntity.badRequest().body(new ErrorResponse(mensaje));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException e) {
        String mensaje = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .orElse("Datos invalidos");
        return ResponseEntity.badRequest().body(new ErrorResponse(mensaje));
    }

    private static String describir(FieldError fe) {
        return fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalido" : fe.getDefaultMessage());
    }
}
