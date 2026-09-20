package com.gamestore.negocio.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Un juego del catalogo. El vendedor NO es una relacion {@code @ManyToOne
 * Usuario}: es un {@code vendedorUsername} de texto plano (clave natural
 * compartida con el sub-dominio usuarios). El nombre para mostrar se resuelve
 * al armar el DTO ({@code catalogo.spi.ResolucionVendedores}).
 *
 * La seccion SI es una relacion: vive en el mismo sub-dominio.
 */
@Entity
@Table(name = "juegos")
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;

    @Size(max = 1000)
    @Column(length = 1000)
    private String descripcion;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Digits(integer = 8, fraction = 2)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * Unidades disponibles. Baja de a 1 en cada compra (ver
     * {@code CompraService}). En 0 el juego queda "sin stock". El
     * {@code columnDefinition} deja las filas viejas en 10 al agregarse la
     * columna (ddl-auto=update sobre una base ya poblada).
     */
    @PositiveOrZero
    @Column(nullable = false, columnDefinition = "integer default 10")
    private int stock = 10;

    /** Ruta publica de la imagen, ej: /uploads/archivo.jpg */
    @Size(max = 255)
    @Column(name = "imagen_url")
    private String imagenUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;

    /** Username del vendedor dueno (clave natural compartida con el sub-dominio usuarios). */
    @NotBlank
    @Size(max = 50)
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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
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
