package com.gamestore.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * SPA fallback: la raíz "/" devuelve index.html (el build de React que
 * vite copia en {@code src/main/resources/static}). El routing del lado
 * del cliente se encarga del resto (usa hash routing, {@code /#/...}).
 *
 * Si todavía no se compiló el frontend, devuelve un texto guía en vez de
 * un 500.
 */
@Configuration
public class SpaFallbackConfig {

    private static final String SIN_BUILD = """
            El frontend no esta compilado. Desde la raiz del repo:
              cd frontend && npm install && npm run build
            La API ya funciona en /api/**.
            """;

    @Bean
    public RouterFunction<ServerResponse> spaRouter() {
        ClassPathResource index = new ClassPathResource("static/index.html");
        return route(
                GET("/").and(path("/api/**").negate()),
                req -> index.exists()
                        ? ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(index)
                        : ServerResponse.ok().contentType(MediaType.TEXT_PLAIN).bodyValue(SIN_BUILD));
    }
}
