package com.gamestore.auth.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * La parte "secreta" del usuario, separada del perfil (que vive en
 * usuarios-service). Aca solo esta lo que hace falta para probar identidad:
 * el hash de la contraseña, el segundo factor y los contadores del bloqueo
 * automatico.
 *
 * Se referencia por {@code username} (no por id): ese es el dato que ambos
 * servicios comparten como clave natural inmutable del usuario. El rol y el
 * flag "habilitado" NO estan aca a proposito -> los trae usuarios-service.
 */
@Entity
@Table(name = "credenciales")
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** Siempre BCrypt. */
    @Column(nullable = false)
    private String passwordHash;

    /** Copia del email, necesaria para casar la cuenta en el login con Google. El canonico lo tiene usuarios-service. */
    @Column
    private String email;

    // ---- Bloqueo automatico por intentos fallidos (LoginAttemptListener) ----
    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    // ---- 2FA (TOTP) ----
    @Column(name = "totp_secret", length = 64)
    private String totpSecret;

    @Column(name = "totp_habilitado", nullable = false)
    private boolean totpHabilitado = false;

    public Credential() {
    }

    public Credential(String username, String passwordHash, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
