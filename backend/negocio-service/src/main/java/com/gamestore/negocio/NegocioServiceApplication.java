package com.gamestore.negocio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * negocio-service: el componente que concentra la logica de negocio de la
 * tienda. Fusiona dos servicios que antes eran procesos separados y se
 * hablaban por HTTP:
 *
 * <ul>
 *   <li>{@code catalogo} - juegos, secciones, compras ficticias e imagenes.</li>
 *   <li>{@code usuarios} - perfil (nombre, email, rol, habilitado), ABM del
 *       ADMIN y estadisticas del dashboard.</li>
 * </ul>
 *
 * Cada sub-dominio es un paquete de primer nivel ({@code com.gamestore.negocio.catalogo}
 * y {@code com.gamestore.negocio.usuario}) con su propia capa model / repository /
 * service / web. Lo unico que cruza el limite entre los dos son dos interfaces
 * chicas ("puertos"):
 *
 * <ul>
 *   <li>{@code catalogo.spi.ResolucionVendedores} - catalogo necesita el
 *       nombre/email/rol del vendedor para armar el JuegoDTO. Lo implementa
 *       {@code usuarios.integration.ResolucionVendedoresJpaAdapter}.</li>
 *   <li>{@code usuario.spi.ConsultaCatalogo} - el ABM necesita saber si un
 *       usuario tiene juegos antes de borrarlo, y el dashboard necesita los
 *       totales. Lo implementa {@code catalogo.integration.ConsultaCatalogoJpaAdapter}.</li>
 * </ul>
 *
 * auth-service sigue siendo remoto (tiene la clave privada y el hash de las
 * credenciales); se lo llama desde {@code usuarios.client.AuthClient}.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class) // no hay login usuario/pass: solo JWT
public class NegocioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NegocioServiceApplication.class, args);
    }
}
