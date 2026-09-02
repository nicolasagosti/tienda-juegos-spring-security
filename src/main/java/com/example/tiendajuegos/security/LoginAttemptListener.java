package com.example.tiendajuegos.security;

import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.repository.UsuarioRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Bloqueo automatico por intentos fallidos.
 *
 * Spring Security publica un evento cada vez que un AuthenticationManager
 * termina de procesar un intento de login -- exito o fracaso -- sin
 * importar de donde vino el pedido. Como tanto el formLogin (UI clasica)
 * como el login manual de AuthApiController (API/React) pasan por el
 * MISMO AuthenticationManager, este listener cubre los dos flujos con
 * una sola implementacion, sin tocar ninguno de los dos controllers.
 */
@Component
public class LoginAttemptListener {

    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_MINUTOS = 15;

    private final UsuarioRepository usuarioRepository;

    public LoginAttemptListener(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        usuarioRepository.findByUsername(username).ifPresent(usuario -> {
            // Si ya esta bloqueada, no seguimos sumando intentos.
            if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
                return;
            }
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            if (usuario.getIntentosFallidos() >= MAX_INTENTOS) {
                usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(BLOQUEO_MINUTOS));
                usuario.setIntentosFallidos(0);
            }
            usuarioRepository.save(usuario);
        });
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (!(event.getAuthentication().getPrincipal() instanceof CustomUserDetails principal)) {
            return; // por ejemplo el login OAuth2 de Google usa otro tipo de principal
        }
        Usuario usuario = principal.getUsuario();
        if (usuario.getIntentosFallidos() > 0 || usuario.getBloqueadoHasta() != null) {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuarioRepository.save(usuario);
        }
    }
}
