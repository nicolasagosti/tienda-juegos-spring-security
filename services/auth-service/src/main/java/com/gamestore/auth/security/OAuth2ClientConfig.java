package com.gamestore.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Registro del cliente OAuth2 de Google a mano, para que el login con
 * Google sea OPCIONAL de verdad (sin credenciales configuradas, la app
 * arranca igual y el resto de auth-service funciona). Identico al monolito.
 */
@Configuration
public class OAuth2ClientConfig {

    @Value("${app.oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${app.oauth2.google.client-secret:}")
    private String googleClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        if (googleClientId.isBlank() || googleClientSecret.isBlank()) {
            return registrationId -> null;
        }
        ClientRegistration google = CommonOAuth2Provider.GOOGLE
                .getBuilder("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }
}
