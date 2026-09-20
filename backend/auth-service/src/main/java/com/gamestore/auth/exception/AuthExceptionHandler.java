package com.gamestore.auth.exception;

import com.gamestore.auth.dto.LoginErrorResponseDto;
import com.gamestore.common.web.dto.ErrorResponseDto;
import com.gamestore.common.web.exception.GlobalExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Solo las excepciones propias del login. Todo lo generico (argumentos
 * invalidos, Bean Validation, dependencias caidas) lo resuelve el
 * {@link GlobalExceptionHandler} de common-web, que ya viene por classpath.
 */
@RestControllerAdvice
public class AuthExceptionHandler {

    /** Password OK pero falta (o esta mal) el codigo 2FA: el frontend necesita el flag para mostrar el 2do paso. */
    @ExceptionHandler(AuthExceptions.DosFactoresRequerido.class)
    public ResponseEntity<LoginErrorResponseDto> handle2fa(AuthExceptions.DosFactoresRequerido e) {
        return ResponseEntity.status(401).body(new LoginErrorResponseDto(e.getMessage(), true));
    }

    @ExceptionHandler({AuthExceptions.NoAutorizado.class, AuthExceptions.CuentaDeshabilitada.class})
    public ResponseEntity<ErrorResponseDto> handleNoAutorizado(RuntimeException e) {
        return ResponseEntity.status(401).body(new ErrorResponseDto(e.getMessage()));
    }
}
