package com.gamestore.auth.web;

import com.gamestore.auth.model.Credential;
import com.gamestore.auth.repository.CredentialRepository;
import com.gamestore.auth.security.TotpService;
import com.gamestore.auth.web.Dtos.*;
import com.gamestore.common.security.AuthPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Activar/desactivar el 2FA de la propia cuenta. Autoservicio para cualquier
 * usuario autenticado, igual que el TotpApiController del monolito. Opera
 * sobre {@link Credential} (no hace falta hablar con usuarios-service).
 */
@RestController
@RequestMapping("/api/auth/2fa")
public class TotpController {

    private final CredentialRepository credentialRepository;
    private final TotpService totpService;

    public TotpController(CredentialRepository credentialRepository, TotpService totpService) {
        this.credentialRepository = credentialRepository;
        this.totpService = totpService;
    }

    private Credential credencialDe(AuthPrincipal principal) {
        return credentialRepository.findByUsername(principal.username())
                .orElseThrow(() -> new IllegalArgumentException("Credencial no encontrada"));
    }

    @GetMapping("/estado")
    public Map<String, Boolean> estado(@AuthenticationPrincipal AuthPrincipal principal) {
        return Map.of("habilitado", credencialDe(principal).isTotpHabilitado());
    }

    @PostMapping("/setup")
    public TotpSetupResponse setup(@AuthenticationPrincipal AuthPrincipal principal) {
        Credential cred = credencialDe(principal);
        String secreto = totpService.generarSecreto();
        cred.setTotpSecret(secreto);
        cred.setTotpHabilitado(false);
        credentialRepository.save(cred);
        return new TotpSetupResponse(secreto, totpService.generarUri(secreto, cred.getUsername()));
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enable(@AuthenticationPrincipal AuthPrincipal principal, @RequestBody TotpCodeRequest req) {
        Credential cred = credencialDe(principal);
        if (cred.getTotpSecret() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Primero inicia el setup del 2FA"));
        }
        if (!totpService.validar(cred.getTotpSecret(), req.codigo())) {
            return ResponseEntity.status(400).body(new ErrorResponse("Codigo incorrecto"));
        }
        cred.setTotpHabilitado(true);
        credentialRepository.save(cred);
        return ResponseEntity.ok(new MensajeResponse("2FA activado correctamente"));
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable(@AuthenticationPrincipal AuthPrincipal principal, @RequestBody TotpCodeRequest req) {
        Credential cred = credencialDe(principal);
        if (!cred.isTotpHabilitado() || !totpService.validar(cred.getTotpSecret(), req.codigo())) {
            return ResponseEntity.status(400).body(new ErrorResponse("Codigo incorrecto"));
        }
        cred.setTotpHabilitado(false);
        cred.setTotpSecret(null);
        credentialRepository.save(cred);
        return ResponseEntity.ok(new MensajeResponse("2FA desactivado"));
    }
}
