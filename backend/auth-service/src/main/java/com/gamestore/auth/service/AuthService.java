package com.gamestore.auth.service;

import com.gamestore.auth.client.UsuarioInfo;
import com.gamestore.auth.client.UsuariosClient;
import com.gamestore.auth.model.Credential;
import com.gamestore.auth.model.RefreshToken;
import com.gamestore.auth.repository.CredentialRepository;
import com.gamestore.auth.security.CredentialUserDetails;
import com.gamestore.auth.security.TotpService;
import com.gamestore.auth.web.Dtos.LoginResponse;
import com.gamestore.auth.web.Dtos.RefreshResponse;
import com.gamestore.auth.web.Dtos.UsuarioDTO;
import com.gamestore.common.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * Orquesta el login/refresh: valida la contraseña localmente (contra
 * {@link Credential}), pide rol y estado a usuarios-service, aplica 2FA y
 * recien ahi emite el par de tokens.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final CredentialRepository credentialRepository;
    private final UsuariosClient usuariosClient;
    private final TotpService totpService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(AuthenticationManager authenticationManager,
                       CredentialRepository credentialRepository,
                       UsuariosClient usuariosClient,
                       TotpService totpService,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.credentialRepository = credentialRepository;
        this.usuariosClient = usuariosClient;
        this.totpService = totpService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse login(String username, String password, String totpCode) {
        Credential credential = autenticarPassword(username, password);

        // Rol + estado "habilitado" viven en usuarios-service.
        UsuarioInfo info = usuariosClient.porUsername(username);
        if (!info.habilitado()) {
            throw new AuthExceptions.CuentaDeshabilitada("Esta cuenta esta deshabilitada");
        }

        if (credential.isTotpHabilitado()) {
            if (totpCode == null || totpCode.isBlank()) {
                throw new AuthExceptions.DosFactoresRequerido("Ingresa el codigo de tu app de autenticacion");
            }
            if (!totpService.validar(credential.getTotpSecret(), totpCode)) {
                throw new AuthExceptions.DosFactoresRequerido("Codigo de verificacion incorrecto");
            }
        }

        String accessToken = jwtService.generar(info.id(), username, info.rol());
        RefreshToken refresh = refreshTokenService.crear(username);
        return new LoginResponse(accessToken, refresh.getToken(), aDto(info));
    }

    public RefreshResponse refresh(String refreshToken) {
        RefreshToken actual = refreshTokenService.buscarValido(refreshToken)
                .orElseThrow(() -> new AuthExceptions.NoAutorizado("Sesion expirada, iniciar sesion de nuevo"));

        UsuarioInfo info = usuariosClient.porUsername(actual.getUsername());
        RefreshToken nuevo = refreshTokenService.rotar(actual);
        String accessToken = jwtService.generar(info.id(), actual.getUsername(), info.rol());
        return new RefreshResponse(accessToken, nuevo.getToken());
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.buscarValido(refreshToken).ifPresent(refreshTokenService::revocar);
        }
    }

    private Credential autenticarPassword(String username, String password) {
        try {
            Authentication result = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            return ((CredentialUserDetails) result.getPrincipal()).getCredential();
        } catch (LockedException e) {
            throw new AuthExceptions.NoAutorizado(
                    "Cuenta bloqueada temporalmente por demasiados intentos fallidos. Proba de nuevo en unos minutos.");
        } catch (BadCredentialsException e) {
            throw new AuthExceptions.NoAutorizado("Usuario o contraseña incorrectos");
        } catch (AuthenticationException e) {
            throw new AuthExceptions.NoAutorizado("Usuario o contraseña incorrectos");
        }
    }

    private static UsuarioDTO aDto(UsuarioInfo i) {
        return new UsuarioDTO(i.id(), i.username(), i.nombreCompleto(), i.email(), i.rol(), i.habilitado());
    }
}
