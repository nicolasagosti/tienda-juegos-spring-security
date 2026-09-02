package com.gamestore.usuarios.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Timeouts para las llamadas a auth-service y catalogo-service. Con
 * {@link RestClientCustomizer} para no romper el scope "prototype" del
 * builder que autoconfigura Spring Boot (usuarios-service tiene DOS
 * clientes distintos que no deben compartir builder).
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClientCustomizer timeoutsCustomizer() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(2))
                .withReadTimeout(Duration.ofSeconds(3));
        return builder -> builder.requestFactory(ClientHttpRequestFactories.get(settings));
    }
}
