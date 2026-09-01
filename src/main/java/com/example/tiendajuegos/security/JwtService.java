package com.example.tiendajuegos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Genera y valida los JWT que usa la API REST (/api/**) para autenticar al
 * frontend React. Es un token FIRMADO (HMAC-SHA256), no encriptado: cualquiera
 * puede leer su contenido (username, rol), pero nadie puede modificarlo o
 * fabricar uno nuevo sin conocer app.jwt.secret. Por eso ese secreto:
 *  - En local/dev tiene un valor por defecto (comodo para probar).
 *  - En produccion (application-prod.properties) NO tiene default: si no
 *    se define la variable de entorno JWT_SECRET, la app directamente no
 *    arranca. Es a proposito (fail-fast) para que nadie despliegue a
 *    produccion con el secreto de ejemplo.
 */
@Component
public class JwtService {

    @Value("${app.jwt.secret:demo-secret-solo-para-desarrollo-local-cambiar-en-produccion-123}")
    private String secretConfigurado;

    @Value("${app.jwt.expiration-ms:86400000}") // 24 horas
    private long expiracionMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secretConfigurado.getBytes(StandardCharsets.UTF_8));
    }

    public String generar(String username, String rol) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(key())
                .compact();
    }

    /** Lanza JwtException si el token esta corrupto, mal firmado o expirado. */
    public Claims validarYObtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
