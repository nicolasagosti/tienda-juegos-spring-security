package com.gamestore.negocio.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Perfil del usuario. Es lo que quedo de la entidad Usuario del monolito
 * despues de sacarle todo lo de autenticacion (hash, TOTP, contadores de
 * bloqueo), que se fue a auth-service.
 *
 * {@code username} es la clave natural compartida con auth-service y, del
 * lado del catalogo, con {@code Juego.vendedorUsername}.
 *
 * Las constraints de {@code jakarta.validation} de la entidad son la ultima
 * red: aunque un DTO de request no valide algo, Hibernate Validator corre en
 * el {@code pre-persist}/{@code pre-update} y no deja guardar basura.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Email
    @Size(max = 255)
    @Column(unique = true)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /** El ADMIN puede deshabilitar una cuenta sin borrarla. auth-service lo consulta en el login. */
    @Column(nullable = false)
    private boolean habilitado = true;

    public Usuario() {
    }

    public Usuario(String username, String nombreCompleto, String email, Rol rol) {
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
    }
}
