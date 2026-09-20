package com.gamestore.negocio.validation;

import com.gamestore.negocio.catalogo.dto.CrearSeccionRequestDto;
import com.gamestore.negocio.catalogo.dto.JuegoFormRequestDto;
import com.gamestore.negocio.usuario.model.Rol;
import com.gamestore.negocio.usuario.dto.ActualizarUsuarioRequestDto;
import com.gamestore.negocio.usuario.dto.CrearUsuarioRequestDto;
import com.gamestore.negocio.usuario.dto.GoogleUsuarioRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que las constraints de {@code jakarta.validation} de los DTOs de
 * request efectivamente disparan. Usa el {@link Validator} de referencia
 * (Hibernate Validator), sin levantar Spring.
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

    // ---------- CrearUsuarioRequestDto ----------

    @Test
    void crearUsuarioRequest_valido_no_tiene_violaciones() {
        var req = new CrearUsuarioRequestDto("vendedor9", "secret1", "Vendedor Nueve", "v9@x.com", Rol.VENDEDOR);

        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void crearUsuarioRequest_marca_username_password_email_y_rol() {
        var req = new CrearUsuarioRequestDto("ab", "123", "  ", "no-es-email", null);

        assertThat(propiedadesInvalidas(req))
                .contains("username", "password", "nombreCompleto", "email", "rol");
    }

    @Test
    void crearUsuarioRequest_acepta_email_nulo() {
        var req = new CrearUsuarioRequestDto("comprador9", "secret1", "Comprador Nueve", null, Rol.COMPRADOR);

        assertThat(validator.validate(req)).isEmpty();
    }

    // ---------- ActualizarUsuarioRequestDto ----------

    @Test
    void actualizarUsuarioRequest_acepta_nuevaPassword_nula_o_vacia() {
        assertThat(validator.validate(new ActualizarUsuarioRequestDto("Nombre", "a@x.com", Rol.COMPRADOR, true, null))).isEmpty();
        assertThat(validator.validate(new ActualizarUsuarioRequestDto("Nombre", "a@x.com", Rol.COMPRADOR, true, ""))).isEmpty();
    }

    @Test
    void actualizarUsuarioRequest_marca_nuevaPassword_corta() {
        var req = new ActualizarUsuarioRequestDto("Nombre", "a@x.com", Rol.COMPRADOR, true, "123");

        assertThat(propiedadesInvalidas(req)).containsExactly("nuevaPassword");
    }

    @Test
    void actualizarUsuarioRequest_marca_nombre_vacio_email_invalido_y_rol_nulo() {
        var req = new ActualizarUsuarioRequestDto(" ", "no-es-email", null, true, "secret1");

        assertThat(propiedadesInvalidas(req)).containsExactlyInAnyOrder("nombreCompleto", "email", "rol");
    }

    // ---------- GoogleUsuarioRequestDto ----------

    @Test
    void googleUsuarioRequest_valido_sin_nombre_no_tiene_violaciones() {
        assertThat(validator.validate(new GoogleUsuarioRequestDto("g@gmail.com", null))).isEmpty();
        assertThat(validator.validate(new GoogleUsuarioRequestDto("g@gmail.com", ""))).isEmpty();
    }

    @Test
    void googleUsuarioRequest_marca_email_vacio_o_invalido() {
        assertThat(propiedadesInvalidas(new GoogleUsuarioRequestDto("", "Nombre"))).containsExactly("email");
        assertThat(propiedadesInvalidas(new GoogleUsuarioRequestDto("no-es-email", "Nombre"))).containsExactly("email");
    }

    // ---------- JuegoFormRequestDto ----------

    @Test
    void juegoFormRequest_valido_no_tiene_violaciones() {
        var form = new JuegoFormRequestDto();
        form.setNombre("Galaxy Raiders");
        form.setPrecio(new BigDecimal("39.99"));
        form.setStock(3);

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    void juegoFormRequest_marca_nombre_vacio_precio_nulo_y_stock_negativo() {
        var form = new JuegoFormRequestDto();
        form.setNombre("   ");
        form.setPrecio(null);
        form.setStock(-2);

        assertThat(propiedadesInvalidas(form)).contains("nombre", "precio", "stock");
    }

    @Test
    void juegoFormRequest_marca_precio_negativo_y_con_demasiados_decimales() {
        var form = new JuegoFormRequestDto();
        form.setNombre("Demo");
        form.setPrecio(new BigDecimal("-1.234"));
        form.setStock(0);

        assertThat(propiedadesInvalidas(form)).contains("precio");
    }

    // ---------- CrearSeccionRequestDto ----------

    @Test
    void crearSeccionRequest_marca_nombre_vacio() {
        assertThat(propiedadesInvalidas(new CrearSeccionRequestDto("", "desc"))).contains("nombre");
    }

    @Test
    void crearSeccionRequest_marca_nombre_demasiado_largo() {
        String largo = "x".repeat(61);

        assertThat(propiedadesInvalidas(new CrearSeccionRequestDto(largo, null))).contains("nombre");
    }
}
