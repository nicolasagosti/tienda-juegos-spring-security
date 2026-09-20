package com.gamestore.gateway;

import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

/**
 * Filtro global:
 *
 *  1) Borra de TODA request entrante los headers que solo pueden venir del
 *     propio gateway/servicios (X-Internal-Token, X-Auth-*) -> un cliente no
 *     puede spoofearlos.
 *  2) Para {@code /api/**} (salvo {@code /api/auth/**}) exige un JWT valido.
 *     Si falta o es invalido, corta con 401 sin molestar a los servicios.
 *  3) Si el token es valido, propaga la identidad hacia adentro como
 *     X-Auth-* (por si un servicio futuro quiere usarla sin re-parsear).
 */
@Component
public class GatewayAuthFilter implements GlobalFilter, Ordered {

    private static final List<String> HEADERS_A_LIMPIAR =
            List.of("X-Internal-Token", "X-Auth-Username", "X-Auth-Uid", "X-Auth-Rol");

    private final JwtValidator jwtValidator;

    public GatewayAuthFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest.Builder mutado = exchange.getRequest().mutate();
        HEADERS_A_LIMPIAR.forEach(h -> mutado.headers(headers -> headers.remove(h)));

        String path = exchange.getRequest().getURI().getPath();
        boolean protegido = path.startsWith("/api/") && !path.startsWith("/api/auth/");

        if (protegido) {
            String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (auth == null || !auth.startsWith("Bearer ")) {
                return rechazar(exchange, "Falta el token de autenticacion");
            }
            Optional<Claims> claims = jwtValidator.validar(auth.substring(7));
            if (claims.isEmpty()) {
                return rechazar(exchange, "Token invalido o expirado");
            }
            Claims c = claims.get();
            mutado.headers(headers -> {
                headers.set("X-Auth-Username", c.getSubject());
                Object uid = c.get("uid");
                if (uid != null) {
                    headers.set("X-Auth-Uid", String.valueOf(uid));
                }
                headers.set("X-Auth-Rol", c.get("rol", String.class));
            });
        }

        return chain.filter(exchange.mutate().request(mutado.build()).build());
    }

    private Mono<Void> rechazar(ServerWebExchange exchange, String mensaje) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = ("{\"mensaje\":\"" + mensaje + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -1; // antes del filtro de ruteo
    }
}
