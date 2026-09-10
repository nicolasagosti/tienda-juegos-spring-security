package com.gamestore.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamestore.auth.client.UsuarioInfo;
import com.gamestore.auth.client.UsuariosClient;
import com.gamestore.auth.repository.CredentialRepository;
import org.apache.commons.codec.binary.Base32;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integracion de los flujos que el split monolito -> microservicios
 * podria haber roto: login, bloqueo por intentos fallidos, refresh con
 * rotacion, cuenta deshabilitada y 2FA (TOTP).
 *
 * usuarios-service se mockea ({@link UsuariosClient}) para aislar la logica
 * de auth-service; el resto (H2, JWT firmado con la clave del classpath,
 * DataInitializer que siembra "admin/admin123", LoginAttemptListener por
 * eventos) corre de verdad.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    CredentialRepository credentialRepository;

    @MockBean
    UsuariosClient usuariosClient;

    @BeforeEach
    void reset() {
        // Cada test arranca con "admin" sin bloqueo y sin 2FA, sin importar el orden.
        credentialRepository.findByUsername("admin").ifPresent(c -> {
            c.setIntentosFallidos(0);
            c.setBloqueadoHasta(null);
            c.setTotpHabilitado(false);
            c.setTotpSecret(null);
            credentialRepository.save(c);
        });
        given(usuariosClient.porUsername("admin"))
                .willReturn(new UsuarioInfo(1L, "admin", "Administrador General", "admin@tiendajuegos.com", "ADMIN", true));
    }

    // ---------- login ----------

    @Test
    void login_ok_devuelveAccessYRefreshToken() throws Exception {
        JsonNode body = login("admin", "admin123", null, 200);
        assertThat(body.get("token").asText()).isNotBlank();
        assertThat(body.get("refreshToken").asText()).isNotBlank();
        assertThat(body.get("usuario").get("rol").asText()).isEqualTo("ADMIN");
    }

    @Test
    void login_passwordIncorrecta_401() throws Exception {
        mvc.perform(loginRequest("admin", "malparido", null))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Usuario o contraseña incorrectos"));
    }

    @Test
    void login_cuentaDeshabilitada_401() throws Exception {
        given(usuariosClient.porUsername("admin"))
                .willReturn(new UsuarioInfo(1L, "admin", "Admin", "a@x.com", "ADMIN", false));

        mvc.perform(loginRequest("admin", "admin123", null))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Esta cuenta esta deshabilitada"));
    }

    // ---------- bloqueo por intentos fallidos ----------

    @Test
    void login_5intentosFallidos_bloqueanLaCuenta() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(loginRequest("admin", "incorrecta" + i, null))
                    .andExpect(status().isUnauthorized());
        }
        // El 6to intento, aun con la contraseña CORRECTA, se rechaza por bloqueo.
        mvc.perform(loginRequest("admin", "admin123", null))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", org.hamcrest.Matchers.containsString("bloqueada")));

        assertThat(credentialRepository.findByUsername("admin").orElseThrow().getBloqueadoHasta())
                .isAfter(java.time.LocalDateTime.now());
    }

    // ---------- refresh con rotacion ----------

    @Test
    void refresh_rota_yElRefreshViejoQuedaInvalido() throws Exception {
        String viejo = login("admin", "admin123", null, 200).get("refreshToken").asText();

        JsonNode r1 = doRefresh(viejo, 200);
        assertThat(r1.get("token").asText()).isNotBlank();
        assertThat(r1.get("refreshToken").asText()).isNotEqualTo(viejo);

        // Reusar el refresh token viejo ahora falla.
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + viejo + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- 2FA (TOTP) ----------

    @Test
    void dosFactores_setupEnableYLuegoElLoginExigeElCodigo() throws Exception {
        String token = login("admin", "admin123", null, 200).get("token").asText();

        // setup -> devuelve el secreto
        MvcResult setup = mvc.perform(post("/api/auth/2fa/setup").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        String secret = om.readTree(setup.getResponse().getContentAsString()).get("secret").asText();
        assertThat(secret).isNotBlank();

        // enable con un codigo valido
        mvc.perform(post("/api/auth/2fa/enable").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + totpActual(secret) + "\"}"))
                .andExpect(status().isOk());

        // ahora el login SIN codigo pide el segundo factor
        mvc.perform(loginRequest("admin", "admin123", null))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.requiere2fa").value(true));

        // login CON codigo valido -> ok
        login("admin", "admin123", totpActual(secret), 200);

        // disable con codigo valido
        mvc.perform(post("/api/auth/2fa/disable").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + totpActual(secret) + "\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void dosFactores_enableConCodigoInvalido_400() throws Exception {
        String token = login("admin", "admin123", null, 200).get("token").asText();
        mvc.perform(post("/api/auth/2fa/setup").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/2fa/enable").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"000000\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- helpers ----------

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder loginRequest(
            String user, String pass, String totp) {
        String json = "{\"username\":\"" + user + "\",\"password\":\"" + pass + "\""
                + (totp != null ? ",\"totpCode\":\"" + totp + "\"" : "") + "}";
        return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json);
    }

    private JsonNode login(String user, String pass, String totp, int expectedStatus) throws Exception {
        MvcResult res = mvc.perform(loginRequest(user, pass, totp))
                .andExpect(status().is(expectedStatus))
                .andReturn();
        return om.readTree(res.getResponse().getContentAsString());
    }

    private JsonNode doRefresh(String refreshToken, int expectedStatus) throws Exception {
        MvcResult res = mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().is(expectedStatus))
                .andReturn();
        return om.readTree(res.getResponse().getContentAsString());
    }

    /** Codigo TOTP valido AHORA para un secreto Base32 (RFC 6238, mismo calculo que TotpService). */
    private static String totpActual(String secretBase32) throws Exception {
        long contador = System.currentTimeMillis() / 1000 / 30;
        byte[] clave = new Base32().decode(secretBase32);
        byte[] datos = new byte[8];
        long c = contador;
        for (int i = 7; i >= 0; i--) {
            datos[i] = (byte) (c & 0xff);
            c >>= 8;
        }
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(clave, "HmacSHA1"));
        byte[] hash = mac.doFinal(datos);
        int offset = hash[hash.length - 1] & 0x0f;
        int bin = ((hash[offset] & 0x7f) << 24) | ((hash[offset + 1] & 0xff) << 16)
                | ((hash[offset + 2] & 0xff) << 8) | (hash[offset + 3] & 0xff);
        return String.format("%06d", bin % 1_000_000);
    }
}
