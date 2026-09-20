package com.gamestore.common.web;

import com.gamestore.common.web.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/**
 * Publica el {@link GlobalExceptionHandler} con solo tener la dependencia
 * {@code common-web} en el classpath. Hace falta el auto-config porque el
 * paquete {@code com.gamestore.common.web} queda fuera del component scan de
 * cada servicio (que arranca en {@code com.gamestore.auth} / {@code .negocio}).
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
