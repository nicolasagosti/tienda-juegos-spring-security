package com.example.tiendajuegos.security;

import com.example.tiendajuegos.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Autentica las peticiones a /api/** que traen "Authorization: Bearer &lt;jwt&gt;".
 *
 * A diferencia del login por sesion (formLogin, UI clasica en Thymeleaf),
 * esto es STATELESS: no hay cookie ni sesion de por medio, el token se
 * valida de cero en cada request. Es el mecanismo pensado para cuando el
 * frontend (Vercel) y el backend (Render/Railway/etc) viven en dominios
 * distintos: evita los dolores de cabeza de cookies cross-site (SameSite,
 * bloqueo de cookies de terceros en Safari/Chrome) y ademas CSRF deja de
 * aplicar (el ataque CSRF explota que el navegador mande cookies "solo";
 * un header Authorization con Bearer nunca se manda solo).
 *
 * Buscamos siempre al Usuario de nuevo en la base (por username) en vez de
 * confiar ciegamente en lo que dice el token: asi, si un ADMIN deshabilita
 * una cuenta, el cambio se nota en el siguiente request aunque el token
 * viejo todavia no haya expirado.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtService.validarYObtenerClaims(token);
                String username = claims.getSubject();

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    usuarioRepository.findByUsername(username).ifPresent(usuario -> {
                        CustomUserDetails principal = new CustomUserDetails(usuario);
                        var authToken = new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    });
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Token invalido, corrupto o expirado: seguimos sin autenticar.
                // Las reglas de autorizacion de SecurityConfig se encargan de
                // devolver 401 JSON si el endpoint pedido lo requeria.
            }
        }

        filterChain.doFilter(request, response);
    }
}
