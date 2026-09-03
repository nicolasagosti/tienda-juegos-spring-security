package com.gamestore.catalogo.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/** Timeouts para la resolucion de vendedores contra usuarios-service. */
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
