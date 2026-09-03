package com.example.tiendajuegos.config;

import com.example.tiendajuegos.security.JwtAuthenticationFilter;
import com.example.tiendajuegos.security.OAuth2LoginSuccessHandler;
import com.example.tiendajuegos.security.UsuarioDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuracion central de Spring Security.
 *
 * La app tiene DOS interfaces que conviven sobre las mismas entidades y el
 * mismo AuthenticationProvider, pero con DOS mecanismos de autenticacion
 * distintos, cada uno pensado para su forma de desplegarse:
 *
 *  - La API REST en JSON bajo /api/** (la consume el frontend React) usa
 *    JWT stateless (header "Authorization: Bearer ..."). Pensado para el
 *    escenario en que el frontend vive en un dominio (Vercel) y el backend
 *    en otro (Render/Railway/etc): sin cookies de por medio, no hay
 *    problemas de SameSite ni de bloqueo de cookies de terceros, y CSRF
 *    directamente no aplica (ver JwtAuthenticationFilter).
 *  - Las vistas clasicas en Thymeleaf bajo /juegos, /admin, /login siguen
 *    usando sesion + formLogin de toda la vida, con su cookie CSRF de
 *    sesion (la que Thymeleaf inyecta solo en los formularios).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /** Origenes permitidos para llamar a /api/** desde un navegador (CORS). Patrones separados por coma. */
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8081}")
    private String corsAllowedOrigins;

    /**
     * BCrypt: algoritmo de hash con "salt" incorporado y factor de costo
     * configurable. Nunca guardamos contraseñas en texto plano.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * El "puente" entre el UserDetailsService (busca el usuario en la BD)
     * y el PasswordEncoder (compara la contraseña). Tanto el login por
     * formulario (Thymeleaf) como el login de AuthApiController (React,
     * que despues emite el JWT) terminan pasando por aca.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UsuarioDetailsServiceImpl usuarioDetailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS solo importa para /api/**: las paginas Thymeleaf las sirve el
     * propio backend (mismo origen, el navegador nunca hace preflight), y
     * en el modo "embebido" (React compilado adentro de este mismo
     * proceso) tampoco aplica por la misma razon. Donde SI hace falta es
     * cuando el React vive en Vercel y pega contra este backend en otro
     * dominio: ahi hay que decirle al navegador explicitamente que
     * origenes estan permitidos.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList(corsAllowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        // No usamos cookies para la API (es JWT en el header), asi que no
        // necesitamos allowCredentials(true) ni sus restricciones extra.
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtAuthenticationFilter,
                                            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) throws Exception {
        AntPathRequestMatcher apiMatcher = new AntPathRequestMatcher("/api/**");

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // --- Reglas de autorizacion por URL ---
            .authorizeHttpRequests(auth -> auth
                // Build de React (index.html + assets/) y recursos estaticos clasicos
                .requestMatchers("/", "/index.html", "/assets/**", "/favicon.svg", "/vite.svg", "/icons.svg").permitAll()
                .requestMatchers("/css/**", "/js/**", "/uploads/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/error", "/403").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // Flujo de login con Google: Spring Security mapea estas rutas
                // solas (OAuth2LoginConfigurer), solo hace falta permitirlas.
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                // API de autenticacion: login/me/logout son publicos a nivel de URL;
                // /me decide 200 vs 401 mirando el SecurityContext (poblado por
                // JwtAuthenticationFilter si vino un Bearer token valido).
                .requestMatchers("/api/auth/**").permitAll()

                // API REST protegida por rol (misma logica que la version Thymeleaf)
                .requestMatchers("/api/admin/**", "/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/juegos").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/juegos/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/juegos/**").hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/secciones").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/secciones/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()

                // UI clasica en Thymeleaf (se mantiene funcionando en paralelo)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/juegos/nuevo", "/juegos/guardar",
                                  "/juegos/editar/**", "/juegos/*/eliminar", "/juegos/*/guardar")
                    .hasAnyRole("VENDEDOR", "ADMIN")
                .requestMatchers("/juegos", "/juegos/**").authenticated()

                .anyRequest().authenticated()
            )

            // --- Login basado en formulario (UI clasica en Thymeleaf) ---
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/juegos", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            // --- Logout (UI clasica; la API con JWT no tiene sesion que cerrar del lado del servidor) ---
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            // --- Login con Google ---
            // Es, por naturaleza, un flujo de redirects de navegador (Google
            // no habla JSON/JWT con nosotros). OAuth2LoginSuccessHandler es
            // el puente: una vez que Google confirma la identidad, generamos
            // nuestros propios tokens y redirigimos de vuelta al frontend.
            // Si GOOGLE_CLIENT_ID/SECRET no estan configurados (ver
            // OAuth2ClientConfig), esto queda inerte sin romper el resto de
            // la app: el boton de Google en el frontend simplemente no
            // funcionaria hasta que se configuren.
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2LoginSuccessHandler)
            )

            // --- Que responder cuando no hay sesion/token valido, o cuando el rol no alcanza ---
            // Para /api/**: JSON + status code, para que React lo maneje con codigo.
            // Para el resto (Thymeleaf): el comportamiento clasico (forward a /403).
            // OJO: a proposito NO usamos .accessDeniedPage(...) aca, porque
            // internamente fija un unico AccessDeniedHandler que pisa cualquier
            // defaultAccessDeniedHandlerFor(...) ya registrado. En cambio
            // registramos dos entradas en el mismo delegador: una especifica
            // para /api/** y otra "catch-all" para el resto, en ese orden.
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(
                        (request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"mensaje\":\"No autenticado\"}");
                        },
                        apiMatcher)
                .defaultAccessDeniedHandlerFor(
                        (request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"mensaje\":\"No tenes permiso para realizar esta accion\"}");
                        },
                        apiMatcher)
                .defaultAccessDeniedHandlerFor(
                        thymeleafAccessDeniedHandler(),
                        new AntPathRequestMatcher("/**"))
            )

            // La consola de H2 usa <frame>, y por defecto Spring Security lo
            // bloquea con X-Frame-Options: DENY. Esto solo se habilita para
            // esta demo; en produccion la consola H2 NO deberia exponerse.
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )

            // --- CSRF ---
            // Se mantiene activo para la UI clasica en Thymeleaf (repositorio
            // por defecto, basado en la sesion: Thymeleaf inyecta el token
            // solo en cada <form th:action>, sin que haga falta JS ni cookies
            // legibles). Se excluye /api/** porque esa API es stateless via
            // JWT en el header Authorization: sin cookie de sesion ambiente,
            // no hay nada que un sitio de terceros pueda "pedir prestado", asi
            // que el ataque que CSRF previene ni siquiera aplica ahi.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**")
            )

            // JwtAuthenticationFilter reemplaza, para /api/**, lo que
            // UsernamePasswordAuthenticationFilter hace para el login por
            // formulario: poblar el SecurityContext a partir de la
            // credencial (ahi es un usuario/contraseña; aca, un token ya
            // validado). Por eso va antes en la cadena.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
            );

        return http.build();
    }

    /** Forward clasico a la pagina /403 en Thymeleaf, usado para todo lo que no sea /api/**. */
    private AccessDeniedHandler thymeleafAccessDeniedHandler() {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        handler.setErrorPage("/403");
        return handler;
    }
}
