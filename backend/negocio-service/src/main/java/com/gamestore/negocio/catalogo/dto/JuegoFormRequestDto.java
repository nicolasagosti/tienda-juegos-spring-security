package com.gamestore.negocio.catalogo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Cuerpo (multipart) del alta y la edicion de un juego. Se enlaza con
 * {@code @Valid @ModelAttribute} en {@code JuegoController}; el archivo de
 * imagen viaja aparte como {@code @RequestPart}.
 *
 * Es un bean mutable (no un record) porque el binder de multipart de Spring
 * MVC lo llena campo a campo.
 */
public class JuegoFormRequestDto {

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @Size(max = 1000)
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal precio;

    /** Unidades iniciales / nuevas. Si el form no lo manda, queda en 0. */
    @PositiveOrZero
    private int stock;

    /** Opcional: si no viene, el juego queda sin seccion. */
    private Long seccionId;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Long getSeccionId() {
        return seccionId;
    }

    public void setSeccionId(Long seccionId) {
        this.seccionId = seccionId;
    }
}
