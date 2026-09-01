package com.example.tiendajuegos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * "Seccion" = categoria del catalogo (Accion, RPG, Deportes, etc).
 * Solo el ADMIN puede crearlas o eliminarlas (requisito "crear secciones").
 */
@Entity
@Table(name = "secciones")
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    public Seccion() {
    }

    public Seccion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
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
}
