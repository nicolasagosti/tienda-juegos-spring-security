package com.example.tiendajuegos.api.controller;

import com.example.tiendajuegos.api.dto.Requests.ErrorResponse;
import com.example.tiendajuegos.api.dto.Requests.MensajeResponse;
import com.example.tiendajuegos.api.dto.Requests.TotpCodeRequest;
import com.example.tiendajuegos.api.dto.Requests.TotpSetupResponse;
import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.repository.UsuarioRepository;
import com.example.tiendajuegos.security.CustomUserDetails;
import com.example.tiendajuegos.security.TotpService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Activar/desactivar el segundo factor (2FA/TOTP) de la propia cuenta.
 * Cualquier usuario autenticado (de cualquier rol) puede activarlo para
 * SU PROPIA cuenta -- no hace falta permiso especial, es autoservicio.
 */
@RestController
@RequestMapping("/api/auth/2fa")
public class TotpApiController {

    private final UsuarioRepository usuarioRepository;
    private final TotpService totpService;

    public TotpApiController(UsuarioRepository usuarioRepository, TotpService totpService) {
        this.usuarioRepository = usuarioRepository;
        this.totpService = totpService;
    }

    @GetMapping("/estado")
    public Map<String, Boolean> estado(@AuthenticationPrincipal CustomUserDetails principal) {
        return Map.of("habilitado", principal.getUsuario().isTotpHabilitado());
    }

    /** Genera un secreto nuevo (pendiente de confirmar) y la URI para armar el QR. */
    @PostMapping("/setup")
    public TotpSetupResponse setup(@AuthenticationPrincipal CustomUserDetails principal) {
        Usuario usuario = principal.getUsuario();
        String secreto = totpService.generarSecreto();
        usuario.setTotpSecret(secreto);
        usuario.setTotpHabilitado(false); // pendiente de confirmar con /enable
        usuarioRepository.save(usuario);

        return new TotpSetupResponse(secreto, totpService.generarUri(secreto, usuario.getUsername()));
    }

    /** Confirma el setup: hace falta un codigo valido para recien ahi activar el 2FA de verdad. */
    @PostMapping("/enable")
    public ResponseEntity<?> enable(@AuthenticationPrincipal CustomUserDetails principal, @RequestBody TotpCodeRequest req) {
        Usuario usuario = principal.getUsuario();
        if (usuario.getTotpSecret() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Primero iniciá el setup del 2FA"));
        }
        if (!totpService.validar(usuario.getTotpSecret(), req.codigo())) {
            return ResponseEntity.status(400).body(new ErrorResponse("Codigo incorrecto"));
        }
        usuario.setTotpHabilitado(true);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new MensajeResponse("2FA activado correctamente"));
    }

    /** Pide el codigo actual (no la contraseña) para desactivar -- confirma que quien desactiva sigue teniendo el segundo factor. */
    @PostMapping("/disable")
    public ResponseEntity<?> disable(@AuthenticationPrincipal CustomUserDetails principal, @RequestBody TotpCodeRequest req) {
        Usuario usuario = principal.getUsuario();
        if (!usuario.isTotpHabilitado() || !totpService.validar(usuario.getTotpSecret(), req.codigo())) {
            return ResponseEntity.status(400).body(new ErrorResponse("Codigo incorrecto"));
        }
        usuario.setTotpHabilitado(false);
        usuario.setTotpSecret(null);
        usuarioRepository.save(usuario);
        return ResponseEntity.ok(new MensajeResponse("2FA desactivado"));
    }
}
