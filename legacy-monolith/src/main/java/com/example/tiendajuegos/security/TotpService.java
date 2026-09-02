package com.example.tiendajuegos.security;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * TOTP (Time-based One-Time Password), RFC 6238 -- el mismo algoritmo que
 * usan Google Authenticator, Authy, etc. Se implementa a mano (en vez de
 * traer una libreria) porque el algoritmo es corto y es justamente lo
 * interesante para entender "como funciona el 2FA por dentro":
 *
 *  1) Se genera un secreto random compartido entre el server y la app
 *     del usuario (se muestra una sola vez, como texto o como QR).
 *  2) Los dos lados calculan, cada 30 segundos, HMAC-SHA1(secreto, tiempo_actual/30)
 *     y se quedan con 6 digitos de ese hash ("dynamic truncation").
 *  3) Si los dos calculan el mismo numero en la misma ventana de 30s,
 *     es porque los dos conocen el mismo secreto -- eso es "el segundo factor".
 *
 * Nadie manda el secreto en cada login (a diferencia de una contraseña):
 * solo se transmitio una vez, al activar el 2FA.
 */
@Service
public class TotpService {

    private static final int DIGITOS = 6;
    private static final int PASO_SEGUNDOS = 30;
    private static final int VENTANA_TOLERANCIA = 1; // acepta +-1 paso (30s) por desfasaje de reloj

    private final Base32 base32 = new Base32();
    private final SecureRandom random = new SecureRandom();

    /** Nuevo secreto random de 20 bytes (160 bits), codificado en Base32 -- el formato que esperan las apps de authenticator. */
    public String generarSecreto() {
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return base32.encodeToString(bytes);
    }

    /** URI otpauth:// para armar el QR que el usuario escanea con Google Authenticator/Authy/etc. */
    public String generarUri(String secreto, String usernameCuenta) {
        String issuer = URLEncoder.encode("GameStore", StandardCharsets.UTF_8);
        String cuenta = URLEncoder.encode(usernameCuenta, StandardCharsets.UTF_8);
        return "otpauth://totp/" + issuer + ":" + cuenta
                + "?secret=" + secreto
                + "&issuer=" + issuer
                + "&algorithm=SHA1&digits=" + DIGITOS + "&period=" + PASO_SEGUNDOS;
    }

    /** true si "codigo" (los 6 digitos que tipeo el usuario) es valido para este secreto en el momento actual. */
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

            // "Dynamic truncation" (RFC 4226): se toma un pedazo de 4 bytes
            // del hash, elegido por sus ultimos 4 bits, y se lo interpreta
            // como numero de 31 bits.
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
