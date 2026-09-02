package com.example.tiendajuegos.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Refresh token de larga duracion, guardado en la base (a diferencia del
 * access token JWT, que es stateless y no se persiste). Guardarlo es lo
 * que permite REVOCARLO: un JWT comun no se puede "invalidar" antes de
 * que expire por su cuenta, pero un refresh token que vive en la base si
 * -- alcanza con marcarlo revocado. Es la pieza que le da sentido real al
 * boton de "Salir" (antes, con JWT puro, el logout no invalidaba nada
 * del lado del servidor).
 *
 * El valor del token es un string random opaco (UUID), no un JWT: no
 * hace falta que lleve informacion adentro, su unico trabajo es ser
 * dificil de adivinar y buscarse en esta tabla.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime expiraEn;

    @Column(nullable = false)
    private boolean revocado = false;

    @Column(nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    public RefreshToken() {
    }

    public RefreshToken(String token, Usuario usuario, LocalDateTime expiraEn) {
        this.token = token;
        this.usuario = usuario;
        this.expiraEn = expiraEn;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public Usuario getUsuario() {
        return usuario;
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
