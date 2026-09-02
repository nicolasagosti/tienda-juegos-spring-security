package com.gamestore.catalogo;

import com.gamestore.catalogo.client.UsuariosClient;
import com.gamestore.catalogo.model.Juego;
import com.gamestore.catalogo.repository.JuegoRepository;
import com.gamestore.common.security.AuthPrincipal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * La autorizacion "a nivel de dato" del catalogo cambio con el split: antes
 * comparaba {@code juego.vendedor.id} contra la entidad Usuario; ahora
 * compara {@code juego.vendedorUsername} contra el {@code username} del JWT.
 * Estos tests fijan ese comportamiento.
 *
 * El JWT se simula inyectando directamente un {@link AuthPrincipal} en el
 * contexto (este servicio no tiene la clave privada para firmar tokens).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogoAuthorizationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    JuegoRepository juegoRepository;

    @MockBean
    UsuariosClient usuariosClient;

    private static Authentication como(String username, String rol) {
        return new UsernamePasswordAuthenticationToken(
                new AuthPrincipal(0, username, rol), null,
                AuthorityUtils.createAuthorityList("ROLE_" + rol));
    }

    private long juegoDe(String vendedorUsername) {
        return juegoRepository.findByVendedorUsername(vendedorUsername).get(0).getId();
    }

    @Test
    void vendedor_puedeBorrarSuPropioJuego() throws Exception {
        long id = juegoDe("vendedor1");
        mvc.perform(delete("/api/juegos/{id}", id).with(authentication(como("vendedor1", "VENDEDOR"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void vendedor_noPuedeBorrarJuegoDeOtroVendedor() throws Exception {
        long ajeno = juegoDe("vendedor2");
        mvc.perform(delete("/api/juegos/{id}", ajeno).with(authentication(como("vendedor1", "VENDEDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_puedeBorrarCualquierJuego() throws Exception {
        long ajeno = juegoDe("vendedor2");
        mvc.perform(delete("/api/juegos/{id}", ajeno).with(authentication(como("admin", "ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void comprador_noPuedePublicar() throws Exception {
        mvc.perform(multipart("/api/juegos")
                        .param("nombre", "Intento")
                        .param("precio", "9.99")
                        .with(authentication(como("comprador1", "COMPRADOR"))))
                .andExpect(status().isForbidden());
    }
}
