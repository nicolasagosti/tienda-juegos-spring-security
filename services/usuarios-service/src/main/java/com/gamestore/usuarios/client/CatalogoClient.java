package com.gamestore.usuarios.client;

import com.gamestore.common.security.InternalTokenFilter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente hacia catalogo-service para dos cosas puntuales:
 *  - los totales de juegos/secciones que van en el dashboard del ADMIN;
 *  - saber si un usuario tiene juegos publicados antes de borrarlo
 *    (equivalente al DataIntegrityViolation que tiraba el monolito).
 */
@Component
public class CatalogoClient {

    /** Totales del catalogo. Si catalogo-service no responde, el dashboard muestra -1 (degradado, no rompe). */
    public record CatalogoStats(long totalJuegos, long totalSecciones) {}

    private final RestClient rest;

    public CatalogoClient(RestClient.Builder builder,
                          @Value("${app.services.catalogo-url}") String baseUrl,
                          @Value("${app.internal.secret}") String internalSecret) {
        this.rest = builder
                .baseUrl(baseUrl)
                .defaultHeader(InternalTokenFilter.HEADER, internalSecret)
                .build();
    }

    @Retry(name = "catalogo")
    @CircuitBreaker(name = "catalogo", fallbackMethod = "statsFallback")
    public CatalogoStats stats() {
        return rest.get().uri("/internal/stats").retrieve().body(CatalogoStats.class);
    }

    @SuppressWarnings("unused")
    private CatalogoStats statsFallback(Throwable t) {
        return new CatalogoStats(-1, -1);
    }

    @Retry(name = "catalogo")
    @CircuitBreaker(name = "catalogo", fallbackMethod = "countFallback")
    public long juegosDeVendedor(String username) {
        Long n = rest.get()
                .uri("/internal/juegos/count-by-vendedor/{u}", username)
                .retrieve()
                .body(Long.class);
        return n == null ? 0 : n;
    }

    @SuppressWarnings("unused")
    private long countFallback(String username, Throwable t) {
        // Ante la duda, asumimos que puede tener juegos: bloquea el borrado (mas seguro).
        throw new ServicioNoDisponibleException("catalogo-service no responde; no se puede verificar el borrado", t);
    }
}
