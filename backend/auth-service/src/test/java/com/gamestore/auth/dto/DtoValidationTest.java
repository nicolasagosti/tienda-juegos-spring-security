package com.gamestore.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que las constraints de {@code jakarta.validation} de los DTOs de
 * request de auth-service efectivamente disparan. Usa el {@link Validator}
 * de referencia (Hibernate Validator), sin levantar Spring.
 */
class DtoValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    private static <T> Set<String> propiedadesInvalidas(T objeto) {
        return validator.validate(objeto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    // ---------- LoginRequestDto ----------

    @Test
    void loginRequest_valido_sin_totp_no_tiene_violaciones() {
        assertThat(validator.validate(new LoginRequestDto("admin", "admin123", null))).isEmpty();
    }

    @Test
    void loginRequest_valido_con_totp_no_tiene_violaciones() {
        assertThat(validator.validate(new LoginRequestDto("admin", "admin123", "123456"))).isEmpty();
    }

    @Test
    void loginRequest_marca_username_y_password_vacios() {
        assertThat(propiedadesInvalidas(new LoginRequestDto("  ", "", null)))
                .containsExactlyInAnyOrder("username", "password");
    }

    @Test
    void loginRequest_marca_totp_no_numerico() {
        assertThat(propiedadesInvalidas(new LoginRequestDto("admin", "admin123", "abc")))
                .containsExactly("totpCode");
    }

    @Test
    void loginRequest_acepta_totp_vacio() {
        // El frontend puede mandar "" en el segundo paso; eso lo resuelve AuthService (requiere2fa).
        assertThat(validator.validate(new LoginRequestDto("admin", "admin123", ""))).isEmpty();
    }

    // ---------- RefreshRequestDto / LogoutRequestDto ----------

    @Test
    void refreshRequest_marca_token_vacio() {
        assertThat(propiedadesInvalidas(new RefreshRequestDto(""))).containsExactly("refreshToken");
    }

    @Test
    void logoutRequest_acepta_token_nulo() {
        assertThat(validator.validate(new LogoutRequestDto(null))).isEmpty();
    }

    // ---------- TotpCodeRequestDto ----------

    @Test
    void totpCodeRequest_acepta_6_digitos() {
        assertThat(validator.validate(new TotpCodeRequestDto("000000"))).isEmpty();
    }

    @Test
    void totpCodeRequest_marca_codigo_vacio_corto_o_con_letras() {
        assertThat(propiedadesInvalidas(new TotpCodeRequestDto(""))).containsExactly("codigo");
        assertThat(propiedadesInvalidas(new TotpCodeRequestDto("12345"))).containsExactly("codigo");
        assertThat(propiedadesInvalidas(new TotpCodeRequestDto("12345a"))).containsExactly("codigo");
    }

    // ---------- CrearCredencialRequestDto ----------

    @Test
    void crearCredencialRequest_valido_no_tiene_violaciones() {
        assertThat(validator.validate(new CrearCredencialRequestDto("vendedor9", "v9@x.com", "secret1"))).isEmpty();
    }

    @Test
    void crearCredencialRequest_acepta_email_vacio() {
        // negocio-service manda "" cuando el usuario no tiene email.
        assertThat(validator.validate(new CrearCredencialRequestDto("vendedor9", "", "secret1"))).isEmpty();
    }

    @Test
    void crearCredencialRequest_marca_username_corto_email_invalido_y_password_corta() {
        assertThat(propiedadesInvalidas(new CrearCredencialRequestDto("ab", "no-es-email", "123")))
                .containsExactlyInAnyOrder("username", "email", "password");
    }

    // ---------- CambiarPasswordRequestDto ----------

    @Test
    void cambiarPasswordRequest_marca_password_corta() {
        assertThat(propiedadesInvalidas(new CambiarPasswordRequestDto("12345"))).containsExactly("password");
    }

    @Test
    void cambiarPasswordRequest_valido_no_tiene_violaciones() {
        assertThat(validator.validate(new CambiarPasswordRequestDto("nueva123"))).isEmpty();
    }
}
