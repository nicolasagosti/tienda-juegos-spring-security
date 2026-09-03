package com.gamestore.catalogo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Diferencia clave con el monolito: el vendedor ya no es una relacion
 * {@code @ManyToOne Usuario} sino un simple {@code vendedorUsername}. El
 * usuario vive en otro servicio y otra base; guardar la FK no tendria
 * sentido. El nombre para mostrar se resuelve contra usuarios-service al
 * armar el DTO.
 */
@Entity
@Table(name = "juegos")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /** Ruta publica de la imagen, ej: /uploads/archivo.jpg */
    @Column(name = "imagen_url")
    private String imagenUrl;

    // La seccion SI sigue siendo una relacion: vive en este mismo servicio.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;

    /** Username del vendedor dueño (clave natural compartida con usuarios-service). */
    @Column(name = "vendedor_username", nullable = false, length = 50)
    private String vendedorUsername;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Juego() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public Seccion getSeccion() {
        return seccion;
    }

    public void setSeccion(Seccion seccion) {
        this.seccion = seccion;
    }

    public String getVendedorUsername() {
        return vendedorUsername;
    }

    public void setVendedorUsername(String vendedorUsername) {
        this.vendedorUsername = vendedorUsername;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
