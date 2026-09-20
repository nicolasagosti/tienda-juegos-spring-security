package com.gamestore.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Protege los endpoints {@code /internal/**} que un servicio expone SOLO
 * para que lo llamen otros servicios (nunca el navegador). Exige el header
 * {@code X-Internal-Token} con un secreto compartido entre servicios.
 *
 * Es deliberadamente simple: en un cluster real esto se reforzaria con
 * network policies / mTLS y el token seria una segunda barrera. Para la
 * demo (docker-compose en una red interna) el header alcanza.
 *
 * Los servicios lo instancian a mano en su SecurityConfig (no es un bean
 * autoconfigurado) porque cada uno decide si tiene o no API interna.
 */
public class InternalTokenFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Internal-Token";

    private final String secretoEsperado;

    public InternalTokenFilter(String secretoEsperado) {
        this.secretoEsperado = secretoEsperado;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recibido = request.getHeader(HEADER);
        if (secretoEsperado == null || secretoEsperado.isBlank() || !secretoEsperado.equals(recibido)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"mensaje\":\"Llamada interna no autorizada\"}");
            return;
        }
        // Marcamos la request como autenticada para que las reglas .authenticated() la dejen pasar.
        var auth = new UsernamePasswordAuthenticationToken(
                "internal-service", null, AuthorityUtils.createAuthorityList("ROLE_INTERNAL"));
        SecurityContextHolder.getContext().setAuthentication(auth);
        chain.doFilter(request, response);
    }
}
