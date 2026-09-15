package com.gamestore.auth.controllers;

import com.gamestore.auth.model.Credential;
import com.gamestore.auth.repository.CredentialRepository;
import com.gamestore.auth.security.TotpService;
import com.gamestore.auth.dto.ErrorResponseDto;
import com.gamestore.auth.dto.MensajeResponseDto;
import com.gamestore.auth.dto.TotpCodeRequestDto;
import com.gamestore.auth.dto.TotpEstadoResponseDto;
import com.gamestore.auth.dto.TotpSetupResponseDto;
import com.gamestore.common.security.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Activar/desactivar el 2FA de la propia cuenta. Autoservicio para cualquier
 * usuario autenticado, igual que el TotpApiController del monolito. Opera
 * sobre {@link Credential} (no hace falta hablar con negocio-service).
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
    public TotpEstadoResponseDto estado(@AuthenticationPrincipal AuthPrincipal principal) {
        return new TotpEstadoResponseDto(credencialDe(principal).isTotpHabilitado());
    }

    @PostMapping("/setup")
    public TotpSetupResponseDto setup(@AuthenticationPrincipal AuthPrincipal principal) {
        Credential cred = credencialDe(principal);
        String secreto = totpService.generarSecreto();
        cred.setTotpSecret(secreto);
        cred.setTotpHabilitado(false);
        credentialRepository.save(cred);
        return new TotpSetupResponseDto(secreto, totpService.generarUri(secreto, cred.getUsername()));
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enable(@AuthenticationPrincipal AuthPrincipal principal,
                                    @Valid @RequestBody TotpCodeRequestDto req) {
        Credential cred = credencialDe(principal);
        if (cred.getTotpSecret() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponseDto("Primero inicia el setup del 2FA"));
        }
        if (!totpService.validar(cred.getTotpSecret(), req.codigo())) {
            return ResponseEntity.status(400).body(new ErrorResponseDto("Codigo incorrecto"));
        }
        cred.setTotpHabilitado(true);
        credentialRepository.save(cred);
        return ResponseEntity.ok(new MensajeResponseDto("2FA activado correctamente"));
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable(@AuthenticationPrincipal AuthPrincipal principal,
                                     @Valid @RequestBody TotpCodeRequestDto req) {
        Credential cred = credencialDe(principal);
        if (!cred.isTotpHabilitado() || !totpService.validar(cred.getTotpSecret(), req.codigo())) {
            return ResponseEntity.status(400).body(new ErrorResponseDto("Codigo incorrecto"));
        }
        cred.setTotpHabilitado(false);
        cred.setTotpSecret(null);
        credentialRepository.save(cred);
        return ResponseEntity.ok(new MensajeResponseDto("2FA desactivado"));
    }
}
