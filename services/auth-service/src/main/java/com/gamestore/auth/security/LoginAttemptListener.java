package com.gamestore.auth.security;

import com.gamestore.auth.model.Credential;
import com.gamestore.auth.repository.CredentialRepository;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Bloqueo automatico por intentos fallidos, igual que en el monolito: 5
 * fallos seguidos -> 15 minutos de bloqueo. Sigue funcionando por eventos
 * del AuthenticationManager, solo que ahora el estado se guarda en
 * {@link Credential} en vez de en la entidad Usuario.
 */
@Component
public class LoginAttemptListener {

    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_MINUTOS = 15;

    private final CredentialRepository credentialRepository;

    public LoginAttemptListener(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        credentialRepository.findByUsername(username).ifPresent(cred -> {
            if (cred.getBloqueadoHasta() != null && cred.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
                return;
            }
            cred.setIntentosFallidos(cred.getIntentosFallidos() + 1);
            if (cred.getIntentosFallidos() >= MAX_INTENTOS) {
                cred.setBloqueadoHasta(LocalDateTime.now().plusMinutes(BLOQUEO_MINUTOS));
                cred.setIntentosFallidos(0);
            }
            credentialRepository.save(cred);
        });
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (!(event.getAuthentication().getPrincipal() instanceof CredentialUserDetails principal)) {
            return; // el login OAuth2 de Google usa otro principal
        }
        Credential cred = principal.getCredential();
        if (cred.getIntentosFallidos() > 0 || cred.getBloqueadoHasta() != null) {
            cred.setIntentosFallidos(0);
            cred.setBloqueadoHasta(null);
            credentialRepository.save(cred);
        }
    }
}
