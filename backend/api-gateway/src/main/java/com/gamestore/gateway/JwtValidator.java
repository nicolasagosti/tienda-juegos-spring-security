package com.gamestore.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

/**
 * Validacion minima de JWT en el gateway: misma clave publica RSA y mismo
 * issuer que usan los servicios (via common-security). Si esto pasa, el
 * request sigue; igual cada servicio vuelve a validar por su cuenta.
 */
@Component
public class JwtValidator {

    private final RSAPublicKey publicKey;
    private final String issuer;

    public JwtValidator(@Value("${app.jwt.public-key-location}") Resource publicKeyPem,
                        @Value("${app.jwt.issuer:gamestore-auth}") String issuer) {
        this.issuer = issuer;
        this.publicKey = leerClavePublica(publicKeyPem);
    }

    public Optional<Claims> validar(String token) {
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token);
            return Optional.of(jws.getPayload());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private static RSAPublicKey leerClavePublica(Resource pem) {
        try {
            String contenido = StreamUtils.copyToString(pem.getInputStream(), StandardCharsets.UTF_8);
            String base64 = contenido
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(base64);
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cargar la clave publica JWT del gateway", e);
        }
    }
}
