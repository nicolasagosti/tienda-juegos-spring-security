package com.gamestore.auth.web;

import com.gamestore.auth.client.ServicioNoDisponibleException;
import com.gamestore.auth.client.UsuarioInfo;
import com.gamestore.auth.client.UsuariosClient;
import com.gamestore.auth.service.AuthService;
import com.gamestore.auth.web.Dtos.*;
import com.gamestore.common.security.AuthPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Login / refresh / logout / "quien soy" para el frontend React, en JSON.
 * Misma forma de request y response que el AuthApiController del monolito.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuariosClient usuariosClient;

    public AuthController(AuthService authService, UsuariosClient usuariosClient) {
        this.authService = authService;
        this.usuariosClient = usuariosClient;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req.username(), req.password(), req.totpCode());
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequest req) {
        authService.logout(req != null ? req.refreshToken() : null);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lo llama el frontend al cargar la pagina. Si usuarios-service esta
     * caido devolvemos igual lo que afirma el token (id, username, rol) en
     * vez de un 503: el usuario puede seguir navegando el catalogo.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal AuthPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new ErrorResponse("No autenticado"));
        }
        try {
            UsuarioInfo i = usuariosClient.porUsername(principal.username());
            return ResponseEntity.ok(new UsuarioDTO(i.id(), i.username(), i.nombreCompleto(), i.email(), i.rol(), i.habilitado()));
        } catch (ServicioNoDisponibleException e) {
            return ResponseEntity.ok(new UsuarioDTO(
                    principal.id(), principal.username(), principal.username(), null, principal.rol(), true));
        }
    }
}
