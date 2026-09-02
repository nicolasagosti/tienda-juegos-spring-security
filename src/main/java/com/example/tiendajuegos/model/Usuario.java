package com.example.tiendajuegos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Guardado siempre con BCrypt, nunca en texto plano. */
    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Email
    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /** Permite al ADMIN deshabilitar una cuenta sin borrarla. */
    @Column(nullable = false)
    private boolean habilitado = true;

    // ---------- Bloqueo automatico por intentos fallidos ----------
    // Distinto de "habilitado": eso lo apaga el ADMIN a mano; esto lo
    // maneja el propio sistema en respuesta a intentos de login fallidos
    // (ver LoginAttemptListener). CustomUserDetails.isAccountNonLocked()
    // usa "bloqueadoHasta" para que Spring Security rechace el login
    // mientras dure el bloqueo, sin necesidad de tocar "habilitado".

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    // ---------- 2FA (TOTP) ----------
    // totpSecret queda null hasta que el usuario arranca el setup; recien
    // se marca totpHabilitado=true cuando confirma un codigo valido (asi
    // no queda "a medio activar" si escanea el QR pero nunca confirma).

    @Column(name = "totp_secret", length = 64)
    private String totpSecret;

    @Column(name = "totp_habilitado", nullable = false)
    private boolean totpHabilitado = false;

    public Usuario() {
    }

    public Usuario(String username, String password, String nombreCompleto, String email, Rol rol) {
        this.username = username;
        this.password = password;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public boolean isTotpHabilitado() {
        return totpHabilitado;
    }

    public void setTotpHabilitado(boolean totpHabilitado) {
        this.totpHabilitado = totpHabilitado;
    }
}
