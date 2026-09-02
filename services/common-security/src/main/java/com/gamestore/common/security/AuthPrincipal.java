package com.gamestore.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Identidad que viaja dentro del JWT y queda como {@code principal} en el
 * SecurityContext de cada servicio. Reemplaza al {@code CustomUserDetails}
 * del monolito: aca NO hay entidad JPA detras, solo lo que el token afirma.
 *
 * @param id       id del usuario (claim "uid"); sirve para comparar
 *                 pertenencia ("es tu juego?") sin ir a la base.
 * @param username claim "sub".
 * @param rol      ADMIN | VENDEDOR | COMPRADOR (claim "rol").
 */
public record AuthPrincipal(long id, String username, String rol) {

    public List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    public boolean isAdmin() {
        return "ADMIN".equals(rol);
    }
}
