package com.gamestore.auth.security;

import com.gamestore.auth.client.UsuarioInfo;
import com.gamestore.auth.client.UsuariosClient;
import com.gamestore.auth.service.CredentialService;
import com.gamestore.auth.service.RefreshTokenService;
import com.gamestore.common.security.JwtService;
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
 * Igual que en el monolito, pero el "buscar o crear usuario" ahora es una
 * llamada a usuarios-service (que decide el rol -> COMPRADOR por defecto).
 * Aca solo nos aseguramos de tener una credencial local y emitimos el par
 * de tokens antes de redirigir al frontend.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private final UsuariosClient usuariosClient;
    private final CredentialService credentialService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public OAuth2LoginSuccessHandler(UsuariosClient usuariosClient,
                                     CredentialService credentialService,
                                     JwtService jwtService,
                                     RefreshTokenService refreshTokenService) {
        this.usuariosClient = usuariosClient;
        this.credentialService = credentialService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String nombre = oauthUser.getAttribute("name");

        UsuarioInfo info = usuariosClient.buscarOCrearGoogle(email, nombre);
        credentialService.asegurarParaGoogle(info.username(), email);

        String accessToken = jwtService.generar(info.id(), info.username(), info.rol());
        String refreshToken = refreshTokenService.crear(info.username()).getToken();

        String destino = frontendUrl + "/#/oauth-callback"
                + "?token=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);
        response.sendRedirect(destino);
    }
}
