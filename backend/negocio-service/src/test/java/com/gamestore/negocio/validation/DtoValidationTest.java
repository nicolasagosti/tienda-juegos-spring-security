package com.gamestore.negocio.validation;

import com.gamestore.negocio.catalogo.web.dto.CrearSeccionRequest;
import com.gamestore.negocio.catalogo.web.dto.JuegoFormRequest;
import com.gamestore.negocio.usuarios.model.Rol;
import com.gamestore.negocio.usuarios.web.dto.CrearUsuarioRequest;
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

    // ---------- CrearUsuarioRequest ----------

    @Test
    void crearUsuarioRequest_valido_no_tiene_violaciones() {
        var req = new CrearUsuarioRequest("vendedor9", "secret1", "Vendedor Nueve", "v9@x.com", Rol.VENDEDOR);

        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void crearUsuarioRequest_marca_username_password_email_y_rol() {
        var req = new CrearUsuarioRequest("ab", "123", "  ", "no-es-email", null);

        assertThat(propiedadesInvalidas(req))
                .contains("username", "password", "nombreCompleto", "email", "rol");
    }

    @Test
    void crearUsuarioRequest_acepta_email_nulo() {
        var req = new CrearUsuarioRequest("comprador9", "secret1", "Comprador Nueve", null, Rol.COMPRADOR);

        assertThat(validator.validate(req)).isEmpty();
    }

    // ---------- JuegoFormRequest ----------

    @Test
    void juegoFormRequest_valido_no_tiene_violaciones() {
        var form = new JuegoFormRequest();
        form.setNombre("Galaxy Raiders");
        form.setPrecio(new BigDecimal("39.99"));
        form.setStock(3);

        assertThat(validator.validate(form)).isEmpty();
    }

    @Test
    void juegoFormRequest_marca_nombre_vacio_precio_nulo_y_stock_negativo() {
        var form = new JuegoFormRequest();
        form.setNombre("   ");
        form.setPrecio(null);
        form.setStock(-2);

        assertThat(propiedadesInvalidas(form)).contains("nombre", "precio", "stock");
    }

    @Test
    void juegoFormRequest_marca_precio_negativo_y_con_demasiados_decimales() {
        var form = new JuegoFormRequest();
        form.setNombre("Demo");
        form.setPrecio(new BigDecimal("-1.234"));
        form.setStock(0);

        assertThat(propiedadesInvalidas(form)).contains("precio");
    }

    // ---------- CrearSeccionRequest ----------

    @Test
    void crearSeccionRequest_marca_nombre_vacio() {
        assertThat(propiedadesInvalidas(new CrearSeccionRequest("", "desc"))).contains("nombre");
    }

    @Test
    void crearSeccionRequest_marca_nombre_demasiado_largo() {
        String largo = "x".repeat(61);

        assertThat(propiedadesInvalidas(new CrearSeccionRequest(largo, null))).contains("nombre");
    }
}
