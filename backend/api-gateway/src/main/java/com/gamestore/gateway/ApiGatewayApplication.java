package com.gamestore.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * api-gateway: lo unico que el navegador ve.
 *
 *  - Rutea {@code /api/**} y {@code /uploads/**} al servicio que corresponde.
 *  - Sirve el build de React (todo lo demas -> index.html).
 *  - Valida el JWT de "primera linea": rechaza rapido lo que no tiene token,
 *    aunque cada servicio vuelve a validarlo por su cuenta (defensa en
 *    profundidad).
 *  - Borra headers sensibles entrantes (X-Internal-Token, X-Auth-*) para que
 *    un cliente no pueda hacerse pasar por otro servicio.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
