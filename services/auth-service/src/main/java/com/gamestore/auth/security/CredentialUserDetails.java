package com.gamestore.auth.security;

import com.gamestore.auth.model.Credential;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adaptador Credential -> UserDetails, solo para que el
 * DaoAuthenticationProvider pueda comparar el hash y aplicar el bloqueo por
 * intentos fallidos.
 *
 * OJO: {@link #isEnabled()} devuelve siempre true. El flag "habilitado" del
 * usuario NO vive en este servicio -> lo chequea AuthService despues del
 * login, contra la respuesta de usuarios-service.
 */
public class CredentialUserDetails implements UserDetails {

    private final Credential credential;

    public CredentialUserDetails(Credential credential) {
        this.credential = credential;
    }

    public Credential getCredential() {
        return credential;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return credential.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return credential.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        LocalDateTime hasta = credential.getBloqueadoHasta();
        return hasta == null || hasta.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
