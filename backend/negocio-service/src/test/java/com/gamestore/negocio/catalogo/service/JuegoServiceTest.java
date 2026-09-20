package com.gamestore.negocio.catalogo.service;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.negocio.catalogo.model.Juego;
import com.gamestore.negocio.catalogo.repository.JuegoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JuegoServiceTest {

    @Mock
    JuegoRepository juegoRepository;
    @Mock
    ImagenStorageService imagenStorageService;

    @InjectMocks
    JuegoService juegoService;

    private static Juego juegoConStock(long id, int stock) {
        Juego j = new Juego();
        j.setId(id);
        j.setNombre("Demo");
        j.setPrecio(new BigDecimal("9.99"));
        j.setStock(stock);
        j.setVendedorUsername("vendedor1");
        return j;
    }

    @Test
    void buscarPorId_inexistente_tira_illegalArgument() {
        when(juegoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> juegoService.buscarPorId(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void crear_clampea_stock_negativo_a_cero_y_guarda_url_de_imagen() {
        when(imagenStorageService.guardar(any())).thenReturn("/uploads/x.png");
        when(juegoRepository.save(any(Juego.class))).thenAnswer(inv -> inv.getArgument(0));

        Juego creado = juegoService.crear("Demo", "desc", new BigDecimal("10.00"),
                -5, null, "vendedor1", null);

        assertThat(creado.getStock()).isZero();
        assertThat(creado.getImagenUrl()).isEqualTo("/uploads/x.png");
        assertThat(creado.getVendedorUsername()).isEqualTo("vendedor1");
    }

    @Test
    void descontarStock_sin_stock_no_guarda_y_tira_illegalArgument() {
        when(juegoRepository.findByIdParaActualizar(1L)).thenReturn(Optional.of(juegoConStock(1L, 0)));

        assertThatThrownBy(() -> juegoService.descontarStock(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");

        verify(juegoRepository, never()).save(any());
    }

    @Test
    void descontarStock_baja_una_unidad() {
        when(juegoRepository.findByIdParaActualizar(1L)).thenReturn(Optional.of(juegoConStock(1L, 3)));
        when(juegoRepository.save(any(Juego.class))).thenAnswer(inv -> inv.getArgument(0));

        Juego actualizado = juegoService.descontarStock(1L);

        assertThat(actualizado.getStock()).isEqualTo(2);
    }

    @Test
    void agregarStock_cantidad_no_positiva_tira_illegalArgument() {
        assertThatThrownBy(() -> juegoService.agregarStock(1L, 0))
                .isInstanceOf(IllegalArgumentException.class);
        verify(juegoRepository, never()).findByIdParaActualizar(any());
    }

    @Test
    void agregarStock_suma_las_unidades_pedidas() {
        when(juegoRepository.findByIdParaActualizar(1L)).thenReturn(Optional.of(juegoConStock(1L, 2)));
        when(juegoRepository.save(any(Juego.class))).thenAnswer(inv -> inv.getArgument(0));

        Juego actualizado = juegoService.agregarStock(1L, 5);

        assertThat(actualizado.getStock()).isEqualTo(7);
    }

    @Test
    void puedeGestionar_admin_siempre_true_aunque_no_sea_el_dueno() {
        Juego ajeno = juegoConStock(1L, 1);
        ajeno.setVendedorUsername("vendedor2");

        assertThat(juegoService.puedeGestionar(ajeno, new AuthPrincipal(1, "admin", "ADMIN"))).isTrue();
    }

    @Test
    void puedeGestionar_vendedor_solo_sus_juegos() {
        Juego propio = juegoConStock(1L, 1);
        propio.setVendedorUsername("vendedor1");

        assertThat(juegoService.puedeGestionar(propio, new AuthPrincipal(2, "vendedor1", "VENDEDOR"))).isTrue();
        assertThat(juegoService.puedeGestionar(propio, new AuthPrincipal(3, "vendedor2", "VENDEDOR"))).isFalse();
    }

    @Test
    void contarDeVendedor_delega_en_el_repo() {
        lenient().when(juegoRepository.countByVendedorUsername("vendedor1")).thenReturn(4L);

        assertThat(juegoService.contarDeVendedor("vendedor1")).isEqualTo(4L);
    }
}
