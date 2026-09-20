package com.gamestore.common.web.exception;

import com.gamestore.common.web.dto.ErrorResponseDto;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * Traduce a {@link ErrorResponseDto} las excepciones que aparecen en TODOS los
 * servicios: argumentos invalidos, conflictos de estado, dependencias caidas y
 * las tres formas en que puede fallar Bean Validation.
 *
 * Lo publica {@code CommonWebAutoConfiguration} con solo tener la dependencia
 * {@code common-web} en el classpath, asi que ningun servicio lo declara.
 *
 * Va con la precedencia mas baja a proposito: si un servicio define su propio
 * {@code @RestControllerAdvice} para la misma excepcion (por ejemplo
 * negocio-service, que mapea {@code IllegalStateException} a 502), gana el
 * suyo y este queda como red de seguridad.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {

    /** Dato que no pasa una regla de negocio (el chequeo explicito, no la constraint). -> 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDto(e.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponseDto(e.getMessage()));
    }

    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponseDto> handleServicioNoDisponible(ServicioNoDisponibleException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponseDto("Servicio no disponible momentaneamente, proba de nuevo en unos segundos"));
    }

    // ---------- Bean Validation ----------
    // Las tres devuelven 400 con el primer mensaje util ("campo: motivo") en vez
    // del stacktrace/ProblemDetail que arma Spring por defecto.

    /** Falla el @Valid de un @RequestBody (los records *RequestDto). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleBodyInvalido(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(GlobalExceptionHandler::describir)
                .orElse(MENSAJE_POR_DEFECTO);
        return ResponseEntity.badRequest().body(new ErrorResponseDto(mensaje));
    }

    /** Falla una constraint sobre un parametro suelto (@RequestParam, @PathVariable) o un @ModelAttribute. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodInvalido(HandlerMethodValidationException e) {
        String mensaje = e.getAllErrors().stream()
                .findFirst()
                .map(err -> err.getDefaultMessage() == null ? MENSAJE_POR_DEFECTO : err.getDefaultMessage())
                .orElse(MENSAJE_POR_DEFECTO);
        return ResponseEntity.badRequest().body(new ErrorResponseDto(mensaje));
    }

    /** Constraints de entidad que Hibernate valida antes de persistir. */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraint(ConstraintViolationException e) {
        String mensaje = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .orElse(MENSAJE_POR_DEFECTO);
        return ResponseEntity.badRequest().body(new ErrorResponseDto(mensaje));
    }

    private static final String MENSAJE_POR_DEFECTO = "Datos invalidos";

    private static String describir(FieldError fe) {
        return fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalido" : fe.getDefaultMessage());
    }
}
