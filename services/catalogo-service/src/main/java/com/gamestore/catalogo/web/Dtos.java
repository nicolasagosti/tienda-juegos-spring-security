package com.gamestore.catalogo.web;

import com.gamestore.catalogo.model.Seccion;

import java.math.BigDecimal;

/** Cuerpos JSON del catalogo. JuegoDTO tiene la misma forma que en el monolito. */
public class Dtos {

    public record SeccionDTO(Long id, String nombre, String descripcion) {
        public static SeccionDTO from(Seccion s) {
            return s == null ? null : new SeccionDTO(s.getId(), s.getNombre(), s.getDescripcion());
        }
    }

    /** El "vendedor" embebido en cada juego. Se resuelve contra usuarios-service. */
    public record VendedorDTO(Long id, String username, String nombreCompleto, String email, String rol, boolean habilitado) {}

    public record JuegoDTO(
            Long id,
            String nombre,
            String descripcion,
            BigDecimal precio,
            String imagenUrl,
            SeccionDTO seccion,
            VendedorDTO vendedor,
            boolean puedeEditar) {
    }

    public record CrearSeccionRequest(String nombre, String descripcion) {}

    public record ErrorResponse(String mensaje) {}
}
