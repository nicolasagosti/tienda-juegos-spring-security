package com.gamestore.common.security;

import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Lee claves RSA en formato PEM (las que genera {@code keys/generate-keys.sh}
 * con openssl) y las convierte en objetos {@link java.security.Key} usables
 * por jjwt. Sin dependencias de BouncyCastle: alcanza con el JDK.
 */
final class PemKeys {

    private PemKeys() {
    }

    static RSAPublicKey readPublicKey(Resource pem) {
        byte[] der = der(readPem(pem), "PUBLIC KEY");
        try {
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Clave publica RSA invalida en " + describe(pem), e);
        }
    }

    static RSAPrivateKey readPrivateKey(Resource pem) {
        byte[] der = der(readPem(pem), "PRIVATE KEY");
        try {
            return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalStateException("Clave privada RSA invalida en " + describe(pem)
                    + " (se espera PKCS#8: '-----BEGIN PRIVATE KEY-----')", e);
        }
    }

    private static String readPem(Resource resource) {
        try {
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la clave en " + describe(resource), e);
        }
    }

    private static byte[] der(String pem, String tipo) {
        String base64 = pem
                .replace("-----BEGIN " + tipo + "-----", "")
                .replace("-----END " + tipo + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }

    private static String describe(Resource r) {
        try {
            return r.getURI().toString();
        } catch (IOException e) {
            return r.getDescription();
        }
    }
}
