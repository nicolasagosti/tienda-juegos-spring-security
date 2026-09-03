package com.example.tiendajuegos.api.controller;

import com.example.tiendajuegos.api.dto.Requests.*;
import com.example.tiendajuegos.api.dto.UsuarioDTO;
import com.example.tiendajuegos.model.RefreshToken;
import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.security.CustomUserDetails;
import com.example.tiendajuegos.security.JwtService;
import com.example.tiendajuegos.security.RefreshTokenService;
import com.example.tiendajuegos.security.TotpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Login/logout/refresh/"quien soy" para el frontend React, en JSON.
 *
 * Flujo de login (con 2FA opcional):
 *  1) Se valida usuario/contraseña como siempre (AuthenticationManager).
 *  2) Si la cuenta tiene 2FA activado, hace falta ADEMAS un totpCode
 *     valido en el mismo pedido (o en un segundo pedido, una vez que el
 *     frontend ve "requiere2fa": true y le muestra al usuario el campo
 *     del codigo).
 *  3) Si todo esta bien, se devuelven DOS tokens: un access token (JWT,
 *     vida corta) y un refresh token (string opaco, vive en la base,
 *     vida larga). El frontend usa el primero en cada pedido y, cuando
 *     expira, pide uno nuevo con el segundo via /api/auth/refresh.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TotpService totpService;

    public AuthApiController(AuthenticationManager authenticationManager,
                              JwtService jwtService,
                              RefreshTokenService refreshTokenService,
                              TotpService totpService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.totpService = totpService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(req.username(), req.password());
            Authentication authResult = authenticationManager.authenticate(authRequest);

            CustomUserDetails principal = (CustomUserDetails) authResult.getPrincipal();
            Usuario usuario = principal.getUsuario();

            if (usuario.isTotpHabilitado()) {
                if (req.totpCode() == null || req.totpCode().isBlank()) {
                    return ResponseEntity.status(401)
                            .body(new LoginErrorResponse("Ingresa el codigo de tu app de autenticacion", true));
                }
                if (!totpService.validar(usuario.getTotpSecret(), req.totpCode())) {
                    return ResponseEntity.status(401)
                            .body(new LoginErrorResponse("Codigo de verificacion incorrecto", true));
                }
            }

            String accessToken = jwtService.generar(usuario.getUsername(), usuario.getRol().name());
            RefreshToken refreshToken = refreshTokenService.crear(usuario);

            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken.getToken(), UsuarioDTO.from(usuario)));
        } catch (LockedException e) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("Cuenta bloqueada temporalmente por demasiados intentos fallidos. Proba de nuevo en unos minutos."));
        } catch (DisabledException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Esta cuenta esta deshabilitada"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Usuario o contraseña incorrectos"));
        }
    }

    /** El frontend llama esto cuando el access token vence (401 en cualquier pedido), sin pedirle credenciales de nuevo al usuario. */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest req) {
        Optional<RefreshToken> actual = refreshTokenService.buscarValido(req.refreshToken());
        if (actual.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(new ErrorResponse("Sesion expirada, iniciar sesion de nuevo"));
        }

        Usuario usuario = actual.get().getUsuario();
        RefreshToken nuevo = refreshTokenService.rotar(actual.get());
        String accessToken = jwtService.generar(usuario.getUsername(), usuario.getRol().name());

        return ResponseEntity.ok(new RefreshResponse(accessToken, nuevo.getToken()));
    }

    /** Revoca el refresh token: a diferencia de un JWT suelto, este SI se puede invalidar del lado del servidor. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequest req) {
        if (req != null && req.refreshToken() != null) {
            refreshTokenService.buscarValido(req.refreshToken()).ifPresent(refreshTokenService::revocar);
        }
        return ResponseEntity.noContent().build();
    }

    /** Lo llama el frontend al cargar la pagina para validar el access token guardado y obtener los datos del usuario. */
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401).body(new ErrorResponse("No autenticado"));
        }
        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(UsuarioDTO.from(principal.getUsuario()));
    }
}
