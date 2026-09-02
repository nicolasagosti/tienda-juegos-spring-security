package com.example.tiendajuegos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Registro del cliente OAuth2 de Google, a mano (no con las propiedades
 * estandar "spring.security.oauth2.client.registration.*") para que el
 * login con Google sea OPCIONAL de verdad.
 *
 * Si hubieramos usado las propiedades estandar de Spring Boot, dejar
 * GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET sin configurar (por ejemplo,
 * mientras todavia no creaste las credenciales en Google Cloud Console)
 * tumba el arranque de TODA la aplicacion -- Spring Boot intenta armar el
 * registro igual y falla porque el client-id esta vacio. Con este bean
 * manual, si faltan las credenciales simplemente no hay ningun proveedor
 * registrado: el boton "Continuar con Google" del frontend no va a
 * funcionar, pero el resto de la app (login normal, JWT, todo lo demas)
 * arranca sin problemas.
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
            // Repositorio "vacio": /oauth2/authorization/google devuelve
            // 404 en vez de tumbar el arranque de toda la app.
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
