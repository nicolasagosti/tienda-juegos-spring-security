package com.gamestore.negocio.config;

import com.gamestore.common.security.InternalTokenFilter;
import com.gamestore.common.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Union de las dos SecurityConfig que tenian catalogo-service y
 * usuarios-service. Sigue siendo 100% stateless: cada request trae un JWT
 * firmado por auth-service (lo valida common-security con la clave publica),
 * o el header {@code X-Internal-Token} si es una llamada de otro servicio.
 *
 * La pertenencia a nivel de dato ("es tu juego?", "no borres tu propio
 * usuario") no se puede expresar por URL: se revisa en el service/controller.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${app.internal.secret}")
    private String internalSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(f -> f.disable())
            .httpBasic(b -> b.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                // API interna que todavia consume auth-service (login, refresh, google).
                .requestMatchers("/internal/**").hasRole("INTERNAL")
                // --- catalogo ---
                .requestMatchers(HttpMethod.POST, "/api/juegos/*/comprar").hasRole("COMPRADOR")
                .requestMatchers(HttpMethod.GET, "/api/compras").hasRole("COMPRADOR")
                .requestMatchers(HttpMethod.POST, "/api/juegos", "/api/juegos/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/juegos/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/juegos/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/secciones").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/secciones/**").hasRole("ADMIN")
                // --- usuarios ---
                .requestMatchers("/api/usuarios/**", "/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write("{\"mensaje\":\"No autenticado\"}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write("{\"mensaje\":\"No tenes permiso para realizar esta accion\"}");
                })
            )
            .addFilterBefore(new InternalTokenFilter(internalSecret), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
