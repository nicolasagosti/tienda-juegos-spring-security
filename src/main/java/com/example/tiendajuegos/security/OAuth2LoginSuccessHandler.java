package com.example.tiendajuegos.security;

import com.example.tiendajuegos.model.Usuario;
import com.example.tiendajuegos.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Que hacer despues de que Google confirma quien es el usuario.
 *
 * El login con Google (spring-boot-starter-oauth2-client) es, por
 * naturaleza, un flujo de REDIRECTS de navegador -- Google no sabe nada
 * de nuestra API JSON ni de JWT. Este handler es el puente: una vez que
 * Spring Security confirma la identidad con Google, nosotros:
 *  1) Buscamos o creamos el Usuario correspondiente en NUESTRA base
 *     (UsuarioService.buscarOCrearDesdeGoogle).
 *  2) Le generamos el mismo par de tokens (access + refresh) que
 *     recibiria si hubiera entrado con usuario/contraseña.
 *  3) Redirigimos de vuelta al FRONTEND (no a la API) con esos tokens en
 *     el fragmento de la URL (despues de "#"): a diferencia de la query
 *     string, el fragmento nunca se manda al servidor en un request HTTP
 *     (no queda en logs de acceso ni en el header Referer), solo lo lee
 *     JavaScript del lado del navegador.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public OAuth2LoginSuccessHandler(UsuarioService usuarioService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String nombre = oauthUser.getAttribute("name");

        Usuario usuario = usuarioService.buscarOCrearDesdeGoogle(email, nombre);

        String accessToken = jwtService.generar(usuario.getUsername(), usuario.getRol().name());
        String refreshToken = refreshTokenService.crear(usuario).getToken();

        String destino = frontendUrl + "/#/oauth-callback"
                + "?token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        response.sendRedirect(destino);
    }
}
