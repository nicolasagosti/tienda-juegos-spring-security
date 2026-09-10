package com.gamestore.auth.client;

import com.gamestore.common.security.InternalTokenFilter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Cliente HTTP hacia la API interna de usuarios-service. Toda llamada pasa
 * por Resilience4j:
 *
 *  - {@code @Retry}: reintenta un par de veces ante un fallo transitorio
 *    (usuarios-service todavia arrancando, hipo de red).
 *  - {@code @CircuitBreaker}: si usuarios-service esta caido de verdad,
 *    despues de N fallos deja de intentar por un rato y va derecho al
 *    fallback -> devolvemos 503 en vez de colgar cada login 3 segundos.
 *
 * Config de los tiempos/umbral: application.properties (resilience4j.*).
 */
@Component
public class UsuariosClient {

    private final RestClient rest;

    public UsuariosClient(RestClient.Builder builder,
                          @Value("${app.services.usuarios-url}") String baseUrl,
                          @Value("${app.internal.secret}") String internalSecret) {
        this.rest = builder
                .baseUrl(baseUrl)
                .defaultHeader(InternalTokenFilter.HEADER, internalSecret)
                .build();
    }

    @Retry(name = "usuarios")
    @CircuitBreaker(name = "usuarios", fallbackMethod = "fallback")
    public UsuarioInfo porUsername(String username) {
        return rest.get()
                .uri("/internal/usuarios/by-username/{u}", username)
                .retrieve()
                .body(UsuarioInfo.class);
    }

    @Retry(name = "usuarios")
    @CircuitBreaker(name = "usuarios", fallbackMethod = "fallback")
    public UsuarioInfo buscarOCrearGoogle(String email, String nombre) {
        return rest.post()
                .uri("/internal/usuarios/google")
                .body(Map.of("email", email, "nombre", nombre == null ? "" : nombre))
                .retrieve()
                .body(UsuarioInfo.class);
    }

    @SuppressWarnings("unused")
    private UsuarioInfo fallback(String arg, Throwable t) {
        throw new ServicioNoDisponibleException("usuarios-service no responde", t);
    }

    @SuppressWarnings("unused")
    private UsuarioInfo fallback(String email, String nombre, Throwable t) {
        throw new ServicioNoDisponibleException("usuarios-service no responde", t);
    }
}
