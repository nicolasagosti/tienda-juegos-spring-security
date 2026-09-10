package com.gamestore.negocio.usuarios.spi;

/**
 * Puerto que el sub-dominio <b>usuarios</b> necesita del sub-dominio
 * <b>catalogo</b>. Antes esto era una llamada HTTP ({@code usuarios.client.CatalogoClient}
 * con circuit breaker); ahora es una interfaz Java que implementa
 * {@code catalogo.integration.ConsultaCatalogoJpaAdapter} contra sus repositorios.
 *
 * Al ser una llamada en memoria dentro de la misma transaccion ya no hay
 * "servicio caido": desaparecen el retry, el fallback en -1 y el 503.
 */
public interface ConsultaCatalogo {

    /** Totales para el dashboard del ADMIN. */
    Totales totales();

    /** Cuantos juegos tiene publicados ese vendedor (para bloquear su borrado). */
    long juegosDeVendedor(String username);

    record Totales(long totalJuegos, long totalSecciones) {
    }
}
