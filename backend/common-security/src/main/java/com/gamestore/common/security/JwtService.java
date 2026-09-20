package com.gamestore.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

/**
 * Genera y valida los access token de la API, ahora firmados con RSA
 * (RS256) en vez del HMAC compartido del monolito.
 *
 * <ul>
 *   <li><b>auth-service</b> lo construye con clave privada + publica: puede
 *       firmar Y validar.</li>
 *   <li><b>gateway, usuarios-service, catalogo-service</b> lo construyen
 *       solo con la publica: pueden validar, no firmar. Si intentan
 *       {@link #generar} explota (fail-fast).</li>
 * </ul>
 *
 * Claims: {@code sub}=username, {@code uid}=id numerico, {@code rol}=rol de
 * negocio, {@code iss}=issuer. Vida corta (15 min); la sesion larga la
 * sostiene el refresh token, que vive en la base de auth-service.
 */
public class JwtService {

    private final RSAPublicKey publicKey;
    private final RSAPrivateKey privateKey; // null salvo en auth-service
    private final long expiracionMs;
    private final String issuer;

    public JwtService(JwtProperties props) {
        if (props.getPublicKeyLocation() == null) {
            throw new IllegalStateException("Falta app.jwt.public-key-location");
        }
        this.publicKey = PemKeys.readPublicKey(props.getPublicKeyLocation());
        this.privateKey = props.getPrivateKeyLocation() != null
                ? PemKeys.readPrivateKey(props.getPrivateKeyLocation())
                : null;
        this.expiracionMs = props.getAccessExpirationMs();
        this.issuer = props.getIssuer();
    }

    public String generar(long uid, String username, String rol) {
        if (privateKey == null) {
            throw new IllegalStateException(
                    "Este servicio no tiene clave privada: no puede emitir JWT (solo auth-service firma).");
        }
        Date ahora = new Date();
        return Jwts.builder()
                .issuer(issuer)
                .subject(username)
                .claim("uid", uid)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /** Lanza JwtException si el token esta corrupto, mal firmado, expirado o con otro issuer. */
    public Jws<Claims> validar(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);
    }

    public AuthPrincipal aPrincipal(Claims claims) {
        Number uid = claims.get("uid", Number.class);
        return new AuthPrincipal(
                uid != null ? uid.longValue() : 0L,
                claims.getSubject(),
                claims.get("rol", String.class));
    }
}
