package com.gamestore.auth.security;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * TOTP (RFC 6238) implementado a mano, sin cambios respecto del monolito.
 * Es el mismo algoritmo que usan Google Authenticator, Authy, etc.
 */
@Service
public class TotpService {

    private static final int DIGITOS = 6;
    private static final int PASO_SEGUNDOS = 30;
    private static final int VENTANA_TOLERANCIA = 1;

    private final Base32 base32 = new Base32();
    private final SecureRandom random = new SecureRandom();

    public String generarSecreto() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32.encodeToString(bytes);
    }

    public String generarUri(String secreto, String usernameCuenta) {
        String issuer = URLEncoder.encode("GameStore", StandardCharsets.UTF_8);
        String cuenta = URLEncoder.encode(usernameCuenta, StandardCharsets.UTF_8);
        return "otpauth://totp/" + issuer + ":" + cuenta
                + "?secret=" + secreto
                + "&issuer=" + issuer
                + "&algorithm=SHA1&digits=" + DIGITOS + "&period=" + PASO_SEGUNDOS;
    }

    public boolean validar(String secreto, String codigo) {
        if (codigo == null || !codigo.matches("\\d{6}")) {
            return false;
        }
        long ventanaActual = System.currentTimeMillis() / 1000 / PASO_SEGUNDOS;
        for (int i = -VENTANA_TOLERANCIA; i <= VENTANA_TOLERANCIA; i++) {
            if (codigo.equals(generarCodigo(secreto, ventanaActual + i))) {
                return true;
            }
        }
        return false;
    }

    private String generarCodigo(String secretoBase32, long contador) {
        try {
            byte[] clave = base32.decode(secretoBase32);
            byte[] datosContador = new byte[8];
            for (int i = 7; i >= 0; i--) {
                datosContador[i] = (byte) (contador & 0xff);
                contador >>= 8;
            }

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(clave, "HmacSHA1"));
            byte[] hash = mac.doFinal(datosContador);

            int offset = hash[hash.length - 1] & 0x0f;
            int binario = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);

            int codigo = binario % (int) Math.pow(10, DIGITOS);
            return String.format("%0" + DIGITOS + "d", codigo);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el codigo TOTP", e);
        }
    }
}
