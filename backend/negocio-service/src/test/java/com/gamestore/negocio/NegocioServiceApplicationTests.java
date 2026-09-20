package com.gamestore.negocio;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Arranca el contexto completo con H2. Sirve, sobre todo, para verificar que
 * el cableado cruzado entre los dos sub-dominios (los adapters que implementan
 * {@code catalogo.spi.ResolucionVendedores} y {@code usuario.spi.ConsultaCatalogo})
 * no genera un ciclo de beans.
 */
@SpringBootTest
class NegocioServiceApplicationTests {

    @Test
    void contextLoads() {
        // vacio a proposito: si el contexto no levanta, el test falla.
    }
}
