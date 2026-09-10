package com.gamestore.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Equivalente servlet al {@code JwtAuthenticationFilter} del monolito, pero
 * SIN tocar la base de datos: la identidad sale entera del token (que ya
 * viene firmado por auth-service con RSA). Lo usan auth-service,
 * usuarios-service y catalogo-service; el gateway hace su propia version
 * reactiva.
 *
 * Si el token falta o es invalido, seguimos sin autenticar y son las reglas
 * de cada SecurityFilterChain las que deciden 401/403.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Jws<Claims> jws = jwtService.validar(header.substring(7));
                AuthPrincipal principal = jwtService.aPrincipal(jws.getPayload());

                var authToken = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.authorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (JwtException | IllegalArgumentException e) {
                // token invalido/expirado: request sigue anonima
            }
        }

        chain.doFilter(request, response);
    }
}
