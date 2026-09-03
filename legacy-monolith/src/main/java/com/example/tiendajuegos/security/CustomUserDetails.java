package com.example.tiendajuegos.security;

import com.example.tiendajuegos.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adaptador entre nuestra entidad Usuario y el contrato UserDetails que
 * exige Spring Security. Aca es donde el "rol" de negocio (ADMIN,
 * VENDEDOR, COMPRADOR) se convierte en una "authority" de Spring con el
 * prefijo ROLE_, que es lo que despues consultan hasRole()/hasAnyRole().
 */
public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Distinto de isEnabled(): esto lo controla el propio sistema en
     * respuesta a intentos de login fallidos (ver LoginAttemptListener),
     * no el ADMIN a mano. DaoAuthenticationProvider revisa esto ANTES de
     * comparar la contraseña, asi que una cuenta bloqueada rechaza el
     * login aunque la contraseña sea correcta.
     */
    @Override
    public boolean isAccountNonLocked() {
        LocalDateTime bloqueadoHasta = usuario.getBloqueadoHasta();
        return bloqueadoHasta == null || bloqueadoHasta.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isHabilitado();
    }
}
