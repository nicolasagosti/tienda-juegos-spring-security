package com.gamestore.auth.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Igual que en el monolito: string opaco (UUID) de larga duracion, guardado
 * en la base para poder REVOCARLO (un JWT suelto no se puede invalidar
 * antes de que expire). La unica diferencia es que ahora apunta al
 * {@code username} en vez de a una entidad Usuario (que ya no vive en este
 * servicio).
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private LocalDateTime expiraEn;

    @Column(nullable = false)
    private boolean revocado = false;

    @Column(nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public RefreshToken() {
    }

    public RefreshToken(String token, String username, LocalDateTime expiraEn) {
        this.token = token;
        this.username = username;
        this.expiraEn = expiraEn;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public boolean isRevocado() {
        return revocado;
    }

    public void setRevocado(boolean revocado) {
        this.revocado = revocado;
    }

    public boolean esValido() {
        return !revocado && expiraEn.isAfter(LocalDateTime.now());
    }
}
