package com.gamestore.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * usuarios-service: dueño del PERFIL y la AUTORIZACION del usuario
 * (nombre, email, rol, habilitado). No sabe nada de contraseñas.
 *
 * - auth-service le pregunta el rol/estado en cada login.
 * - El ABM del ADMIN entra por aca; la parte de credencial se delega a
 *   auth-service (ver AuthClient).
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class) // no hay login usuario/pass: solo JWT
public class UsuariosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosServiceApplication.class, args);
    }
}
