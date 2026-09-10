package com.gamestore.negocio.catalogo.spi;

import java.util.Collection;
import java.util.Map;

/**
 * Puerto que el sub-dominio <b>catalogo</b> necesita del sub-dominio
 * <b>usuarios</b>: para armar cada {@code JuegoDto} hay que pintar el
 * vendedor (nombre, email, rol) a partir de su {@code username}.
 *
 * Antes era una llamada HTTP con circuit breaker ({@code catalogo.client.UsuariosClient});
 * ahora es esta interfaz, que implementa
 * {@code usuarios.integration.ResolucionVendedoresJpaAdapter} contra el
 * {@code UsuarioRepository}. El sub-dominio catalogo NO conoce la entidad
 * {@code Usuario}: solo este contrato.
 */
public interface ResolucionVendedores {

    /** Trae en una sola pasada todos los vendedores pedidos, indexados por username. */
    Map<String, Vendedor> porUsernames(Collection<String> usernames);

    /**
     * Datos del vendedor que el catalogo necesita mostrar. Es un DTO del
     * puerto, no la entidad de usuarios.
     */
    record Vendedor(Long id, String username, String nombreCompleto, String email, String rol, boolean habilitado) {

        /** Vendedor "degradado": solo el username, cuando no hay un perfil que resolver. */
        public static Vendedor degradado(String username) {
            return new Vendedor(null, username, username, null, "VENDEDOR", true);
        }
    }
}
