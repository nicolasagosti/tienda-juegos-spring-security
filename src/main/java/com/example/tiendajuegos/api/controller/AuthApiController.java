package com.example.tiendajuegos.api.controller;

import com.example.tiendajuegos.api.dto.Requests.ErrorResponse;
import com.example.tiendajuegos.api.dto.Requests.LoginRequest;
import com.example.tiendajuegos.api.dto.Requests.LoginResponse;
import com.example.tiendajuegos.api.dto.UsuarioDTO;
import com.example.tiendajuegos.security.CustomUserDetails;
import com.example.tiendajuegos.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Login/logout/"quien soy" para el frontend React, en JSON.
 *
 * A diferencia de la primera version de este controller (que guardaba la
 * sesion en una cookie HttpSession), esta usa JWT stateless: no hay nada
 * que persistir del lado del servidor. El AuthenticationManager (mismo
 * DaoAuthenticationProvider de siempre: UsuarioDetailsServiceImpl +
 * PasswordEncoder) sigue siendo quien valida usuario/contraseña; lo unico
 * que cambia es que, si es correcto, en vez de abrir una sesion HTTP le
 * devolvemos al cliente un token firmado que el mismo va a reenviar en
 * cada pedido futuro.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthApiController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            Authentication authRequest = new UsernamePasswordAuthenticationToken(req.username(), req.password());
            Authentication authResult = authenticationManager.authenticate(authRequest);

            CustomUserDetails principal = (CustomUserDetails) authResult.getPrincipal();
            String token = jwtService.generar(principal.getUsername(), principal.getUsuario().getRol().name());

            return ResponseEntity.ok(new LoginResponse(token, UsuarioDTO.from(principal.getUsuario())));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Usuario o contraseña incorrectos, o la cuenta esta deshabilitada"));
        }
    }

    /**
     * Con JWT stateless no hay nada que invalidar del lado del servidor
     * (no guardamos sesion ni lista de tokens emitidos): el frontend
     * simplemente descarta el token guardado. Se mantiene el endpoint por
     * prolijidad/simetria con el flujo de login.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    /** Lo llama el frontend al cargar la pagina para validar el token guardado y obtener los datos del usuario. */
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
