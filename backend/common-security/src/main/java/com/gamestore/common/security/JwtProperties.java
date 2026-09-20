package com.gamestore.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * Config de los JWT, prefijo {@code app.jwt} en el application.properties de
 * cada servicio.
 *
 * <ul>
 *   <li>{@code public-key-location}: SIEMPRE presente. Con esto se valida la
 *       firma de los tokens entrantes.</li>
 *   <li>{@code private-key-location}: SOLO en auth-service. Con esto se
 *       firman los tokens nuevos. Si falta, {@link JwtService#generar} tira
 *       IllegalStateException (fail-fast: un servicio que no deberia emitir
 *       tokens no puede hacerlo por accidente).</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Clave publica RSA en PEM (X.509 SubjectPublicKeyInfo). Ej: {@code classpath:jwt-public.pem}. */
    private Resource publicKeyLocation;

    /** Clave privada RSA en PEM (PKCS#8). Solo auth-service. Puede ser null. */
    private Resource privateKeyLocation;

    /** Vida del access token en milisegundos (15 min por defecto, igual que el monolito). */
    private long accessExpirationMs = 900_000L;

    /** Va en el claim "iss"; los validadores lo exigen. */
    private String issuer = "gamestore-auth";

    public Resource getPublicKeyLocation() {
        return publicKeyLocation;
    }

    public void setPublicKeyLocation(Resource publicKeyLocation) {
        this.publicKeyLocation = publicKeyLocation;
    }

    public Resource getPrivateKeyLocation() {
        return privateKeyLocation;
    }

    public void setPrivateKeyLocation(Resource privateKeyLocation) {
        this.privateKeyLocation = privateKeyLocation;
    }

    public long getAccessExpirationMs() {
        return accessExpirationMs;
    }

    public void setAccessExpirationMs(long accessExpirationMs) {
        this.accessExpirationMs = accessExpirationMs;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
