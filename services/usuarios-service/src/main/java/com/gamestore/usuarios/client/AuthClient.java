package com.gamestore.usuarios.client;

import com.gamestore.common.security.InternalTokenFilter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Cliente hacia la API interna de auth-service para la parte "credencial"
 * del ABM de usuarios: cuando el ADMIN crea/edita/borra un usuario, el
 * perfil se guarda local y la credencial se delega aca.
 *
 * NO lleva circuit breaker con fallback silencioso a proposito: si
 * auth-service no responde en un alta, queremos que la operacion FALLE (y
 * usuarios-service compense borrando el perfil), no que quede un usuario a
 * medio crear.
 */
@Component
public class AuthClient {

    private final RestClient rest;

    public AuthClient(RestClient.Builder builder,
                      @Value("${app.services.auth-url}") String baseUrl,
                      @Value("${app.internal.secret}") String internalSecret) {
        this.rest = builder
                .baseUrl(baseUrl)
                .defaultHeader(InternalTokenFilter.HEADER, internalSecret)
                .build();
    }

    @Retry(name = "auth")
    public void crearCredencial(String username, String email, String password) {
        rest.post()
                .uri("/internal/credenciales")
                .body(Map.of("username", username, "email", email == null ? "" : email, "password", password))
                .retrieve()
                .toBodilessEntity();
    }

    @Retry(name = "auth")
    public void cambiarPassword(String username, String nuevaPassword) {
        rest.put()
                .uri("/internal/credenciales/{u}/password", username)
                .body(Map.of("password", nuevaPassword))
                .retrieve()
                .toBodilessEntity();
    }

    @Retry(name = "auth")
    public void eliminarCredencial(String username) {
        rest.delete()
                .uri("/internal/credenciales/{u}", username)
                .retrieve()
                .toBodilessEntity();
    }
}
