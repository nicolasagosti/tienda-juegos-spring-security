package com.gamestore.negocio.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Compra ficticia de un juego por un COMPRADOR. No hay pago ni envio: solo
 * deja constancia de que ese usuario "se quedo" con el juego.
 *
 * Guarda una foto del juego (nombre, precio, imagen) al momento de comprar,
 * asi "Mis compras" sigue teniendo sentido aunque el vendedor borre o cambie
 * el juego despues.
 */
@Entity
@Table(
        name = "compras",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_compra_juego_comprador",
                columnNames = {"juego_id", "comprador_username"}))
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "juego_id", nullable = false)
    private Long juegoId;

    @Column(name = "nombre_juego", nullable = false, length = 120)
    private String nombreJuego;

    /** Ruta publica de la imagen al momento de comprar. */
    @Column(name = "imagen_url")
    private String imagenUrl;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /** Username del comprador (claim "sub" del JWT). */
    @Column(name = "comprador_username", nullable = false, length = 50)
    private String compradorUsername;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra = LocalDateTime.now();

    public Compra() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getJuegoId() {
        return juegoId;
    }

    public void setJuegoId(Long juegoId) {
        this.juegoId = juegoId;
    }

    public String getNombreJuego() {
        return nombreJuego;
    }

    public void setNombreJuego(String nombreJuego) {
        this.nombreJuego = nombreJuego;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getCompradorUsername() {
        return compradorUsername;
    }

    public void setCompradorUsername(String compradorUsername) {
        this.compradorUsername = compradorUsername;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }
}
