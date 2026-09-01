package com.example.tiendajuegos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test de "humo": simplemente levanta todo el contexto de Spring
 * (incluida la cadena de filtros de seguridad) y verifica que no
 * explote nada al arrancar.
 */
@SpringBootTest
class TiendaJuegosApplicationTests {

    @Test
    void contextLoads() {
    }
}
