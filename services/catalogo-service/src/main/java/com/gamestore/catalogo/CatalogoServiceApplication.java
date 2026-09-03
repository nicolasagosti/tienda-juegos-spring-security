package com.gamestore.catalogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * catalogo-service: juegos y secciones.
 *
 * El juego ya NO tiene una FK a Usuario: guarda {@code vendedorUsername}
 * como texto plano. Para pintar el vendedor en el DTO le pregunta a
 * usuarios-service; para decidir "podes editar este juego" usa el
 * username/rol que vienen en el JWT.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class) // no hay login usuario/pass: solo JWT
public class CatalogoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogoServiceApplication.class, args);
    }
}
