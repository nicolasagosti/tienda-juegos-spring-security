package com.gamestore.auth.controllers;

import com.gamestore.auth.service.CredentialService;
import com.gamestore.auth.dto.CambiarPasswordRequestDto;
import com.gamestore.auth.dto.CrearCredencialRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API interna (solo la llama negocio-service, con el header X-Internal-Token
 * que valida InternalTokenFilter). NUNCA se expone por el gateway.
 *
 * Aunque el cliente sea "de confianza", los cuerpos se validan igual: un
 * bug del otro lado no puede dejar una credencial sin username o con una
 * contrasena vacia.
 */
@RestController
@RequestMapping("/internal/credenciales")
public class InternalCredencialesController {

    private final CredentialService credentialService;

    public InternalCredencialesController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping
    public ResponseEntity<Void> crear(@Valid @RequestBody CrearCredencialRequestDto req) {
        credentialService.crear(req.username(), req.email(), req.password());
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{username}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable String username,
                                                @Valid @RequestBody CambiarPasswordRequestDto req) {
        credentialService.cambiarPassword(username, req.password());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> eliminar(@PathVariable String username) {
        credentialService.eliminar(username);
        return ResponseEntity.noContent().build();
    }
}
