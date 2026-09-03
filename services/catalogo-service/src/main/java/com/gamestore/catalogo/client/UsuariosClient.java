package com.gamestore.catalogo.client;

import com.gamestore.catalogo.web.Dtos.VendedorDTO;
import com.gamestore.common.security.InternalTokenFilter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve el vendedor de cada juego contra usuarios-service. Si ese
 * servicio no responde, NO rompemos el catalogo: devolvemos un vendedor
 * "degradado" con solo el username (fallback del circuit breaker).
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

    /** Trae en una sola llamada todos los vendedores pedidos, indexados por username. */
    @Retry(name = "usuarios")
    @CircuitBreaker(name = "usuarios", fallbackMethod = "porUsernamesFallback")
    public Map<String, VendedorDTO> porUsernames(Collection<String> usernames) {
        if (usernames.isEmpty()) {
            return Map.of();
        }
        List<VendedorDTO> lista = rest.get()
                .uri(uri -> uri.path("/internal/usuarios")
                        .queryParam("usernames", String.join(",", usernames))
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return lista == null ? Map.of()
                : lista.stream().collect(Collectors.toMap(VendedorDTO::username, Function.identity()));
    }

    @SuppressWarnings("unused")
    private Map<String, VendedorDTO> porUsernamesFallback(Collection<String> usernames, Throwable t) {
        return usernames.stream().collect(Collectors.toMap(
                Function.identity(),
                u -> new VendedorDTO(null, u, u, null, "VENDEDOR", true)));
    }

    /** Vendedor degradado cuando usuarios-service no lo devolvio. */
    public static VendedorDTO degradado(String username) {
        return new VendedorDTO(null, username, username, null, "VENDEDOR", true);
    }
}
