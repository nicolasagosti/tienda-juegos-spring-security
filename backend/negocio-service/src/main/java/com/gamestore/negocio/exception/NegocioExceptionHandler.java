package com.gamestore.negocio.exception;

import com.gamestore.common.web.dto.ErrorResponseDto;
import com.gamestore.common.web.exception.GlobalExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Solo lo que es propio de negocio-service: las fallas de JPA, el
 * {@code @PreAuthorize} (este es el unico servicio con method security) y la
 * subida de imagenes. El resto (400 de Bean Validation, 409 de
 * ConflictException, 503 de ServicioNoDisponibleException) lo resuelve el
 * {@link GlobalExceptionHandler} de common-web.
 */
@RestControllerAdvice
public class NegocioExceptionHandler {

    /** Compensacion fallida en un alta de usuario (auth-service no respondio). */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponseDto(e.getMessage()));
    }

    /** FK o unique que salta en el flush: no exponemos el detalle del esquema. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto("No se puede completar: hay datos relacionados que lo impiden"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDto("No tenes permiso para realizar esta accion"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponseDto("La imagen es demasiado grande (maximo 5MB)"));
    }
}
