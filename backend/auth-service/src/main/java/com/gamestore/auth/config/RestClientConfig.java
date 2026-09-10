package com.gamestore.auth.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Timeouts cortos para las llamadas entre servicios: si un dependiente no
 * contesta rapido, preferimos fallar (y que Resilience4j abra el circuito).
 *
 * Se hace con un {@link RestClientCustomizer} y NO con un bean
 * {@code RestClient.Builder} propio: el builder que autoconfigura Spring
 * Boot es "prototype" (uno nuevo por punto de inyeccion); pisarlo con un
 * singleton hace que dos clientes compartan y se pisen el baseUrl/headers.
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
