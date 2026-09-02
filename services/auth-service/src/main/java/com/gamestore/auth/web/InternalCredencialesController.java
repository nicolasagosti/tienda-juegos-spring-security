package com.gamestore.auth.web;

import com.gamestore.auth.service.CredentialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API interna (solo la llama usuarios-service, con el header X-Internal-Token
 * que valida InternalTokenFilter). NUNCA se expone por el gateway.
 */
@RestController
@RequestMapping("/internal/credenciales")
public class InternalCredencialesController {

    private final CredentialService credentialService;

    public InternalCredencialesController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping
    public ResponseEntity<Void> crear(@RequestBody Map<String, String> body) {
        credentialService.crear(body.get("username"), body.get("email"), body.get("password"));
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{username}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable String username, @RequestBody Map<String, String> body) {
        credentialService.cambiarPassword(username, body.get("password"));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> eliminar(@PathVariable String username) {
        credentialService.eliminar(username);
        return ResponseEntity.noContent().build();
    }
}
