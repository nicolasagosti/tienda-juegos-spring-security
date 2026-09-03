package com.gamestore.usuarios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Perfil del usuario. Es lo que quedo de la entidad Usuario del monolito
 * despues de sacarle todo lo de autenticacion (hash, TOTP, contadores de
 * bloqueo), que se fue a auth-service.
 *
 * {@code username} es la clave natural compartida con auth-service.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Email
    @Column(unique = true)
    private String email;

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
