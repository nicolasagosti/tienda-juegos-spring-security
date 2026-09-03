package com.gamestore.catalogo.config;

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
 * Mismas reglas de rol que el monolito para el catalogo, pero stateless y
 * validando el JWT en vez de la sesion. La pertenencia ("es tu juego?") se
 * revisa en el controller/servicio (no se puede expresar por URL).
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
                .requestMatchers("/internal/**").hasRole("INTERNAL")
                .requestMatchers(HttpMethod.POST, "/api/juegos", "/api/juegos/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/juegos/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/juegos/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/secciones").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/secciones/**").hasRole("ADMIN")
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
