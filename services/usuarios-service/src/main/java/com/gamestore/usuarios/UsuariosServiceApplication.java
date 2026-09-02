package com.gamestore.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * usuarios-service: dueño del PERFIL y la AUTORIZACION del usuario
 * (nombre, email, rol, habilitado). No sabe nada de contraseñas.
 *
 * - auth-service le pregunta el rol/estado en cada login.
 * - El ABM del ADMIN entra por aca; la parte de credencial se delega a
 *   auth-service (ver AuthClient).
 */
@SpringBootApplication
public class UsuariosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosServiceApplication.class, args);
    }
}
