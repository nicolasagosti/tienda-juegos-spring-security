package com.gamestore.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * auth-service: lo unico que sabe de contraseñas.
 *
 * Dueño de: hash de credenciales, emision y firma de los JWT (clave privada
 * RSA), refresh tokens (revocables, en su propia base), 2FA TOTP, bloqueo
 * por intentos fallidos y el flujo de login con Google.
 *
 * NO es dueño del perfil ni del rol del usuario: eso vive en
 * usuarios-service, al que este servicio le pregunta por REST durante el
 * login (ver UsuariosClient).
 */
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
