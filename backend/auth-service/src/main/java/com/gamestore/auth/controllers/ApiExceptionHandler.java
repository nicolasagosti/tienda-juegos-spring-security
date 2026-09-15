package com.gamestore.auth.controllers;

import com.gamestore.auth.client.ServicioNoDisponibleException;
import com.gamestore.auth.service.AuthExceptions;
import com.gamestore.auth.dto.ErrorResponseDto;
import com.gamestore.auth.dto.LoginErrorResponseDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Traduce las excepciones de negocio a JSON, como el GlobalApiExceptionHandler
 * del monolito. Los handlers de Bean Validation devuelven 400 con el primer
 * mensaje util ({@code "campo: motivo"}), igual que en negocio-service.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthExceptions.DosFactoresRequerido.class)
    public ResponseEntity<LoginErrorResponseDto> handle2fa(AuthExceptions.DosFactoresRequerido e) {
        return ResponseEntity.status(401).body(new LoginErrorResponseDto(e.getMessage(), true));
    }

    @ExceptionHandler({AuthExceptions.NoAutorizado.class, AuthExceptions.CuentaDeshabilitada.class})
    public ResponseEntity<ErrorResponseDto> handleNoAutorizado(RuntimeException e) {
        return ResponseEntity.status(401).body(new ErrorResponseDto(e.getMessage()));
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponseDto> handleDown(ServicioNoDisponibleException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponseDto("Servicio no disponible momentaneamente, proba de nuevo en unos segundos"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDto(e.getMessage()));
    }

    // ---------- Bean Validation ----------

    /** Falla el @Valid de un @RequestBody (records de request en dto). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleBodyInvalido(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(ApiExceptionHandler::describir)
                .orElse("Datos invalidos");
        return ResponseEntity.badRequest().body(new ErrorResponseDto(mensaje));
    }

    /** Falla una constraint sobre un parametro suelto del handler (@RequestParam, @PathVariable). */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodInvalido(HandlerMethodValidationException e) {
        String mensaje = e.getAllErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage() == null ? "Datos invalidos" : err.getDefaultMessage())
                .orElse("Datos invalidos");
        return ResponseEntity.badRequest().body(new ErrorResponseDto(mensaje));
    }

    /** Constraints de entidad (Credential / RefreshToken) que Hibernate corre antes de persistir. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraint(ConstraintViolationException e) {
        String mensaje = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse("Datos invalidos");
        return ResponseEntity.badRequest().body(new ErrorResponseDto(mensaje));
    }

    private static String describir(FieldError fe) {
        return fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalido" : fe.getDefaultMessage());
    }
}
