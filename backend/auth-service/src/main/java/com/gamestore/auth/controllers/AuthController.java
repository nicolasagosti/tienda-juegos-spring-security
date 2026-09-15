package com.gamestore.auth.controllers;

import com.gamestore.auth.client.ServicioNoDisponibleException;
import com.gamestore.auth.client.UsuarioInfo;
import com.gamestore.auth.client.UsuariosClient;
import com.gamestore.auth.service.AuthService;
import com.gamestore.auth.dto.ErrorResponseDto;
import com.gamestore.auth.dto.LoginRequestDto;
import com.gamestore.auth.dto.LoginResponseDto;
import com.gamestore.auth.dto.LogoutRequestDto;
import com.gamestore.auth.dto.RefreshRequestDto;
import com.gamestore.auth.dto.RefreshResponseDto;
import com.gamestore.auth.dto.UsuarioDto;
import com.gamestore.common.security.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Login / refresh / logout / "quien soy" para el frontend React, en JSON.
 * Misma forma de request y response que el AuthApiController del monolito.
 *
 * Los cuerpos se validan con {@code @Valid} (constraints en {@code dto});
 * un cuerpo invalido responde 400 via {@link ApiExceptionHandler} antes de
 * llegar al servicio.
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
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto req) {
        return authService.login(req.username(), req.password(), req.totpCode());
    }

    @PostMapping("/refresh")
    public RefreshResponseDto refresh(@Valid @RequestBody RefreshRequestDto req) {
        return authService.refresh(req.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody(required = false) LogoutRequestDto req) {
        authService.logout(req != null ? req.refreshToken() : null);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lo llama el frontend al cargar la pagina. Si negocio-service esta
     * caido devolvemos igual lo que afirma el token (id, username, rol) en
     * vez de un 503: el usuario puede seguir navegando el catalogo.
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal AuthPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(new ErrorResponseDto("No autenticado"));
        }
        try {
            UsuarioInfo i = usuariosClient.porUsername(principal.username());
            return ResponseEntity.ok(new UsuarioDto(i.id(), i.username(), i.nombreCompleto(), i.email(), i.rol(), i.habilitado()));
        } catch (ServicioNoDisponibleException e) {
            return ResponseEntity.ok(new UsuarioDto(
                    principal.id(), principal.username(), principal.username(), null, principal.rol(), true));
        }
    }
}
