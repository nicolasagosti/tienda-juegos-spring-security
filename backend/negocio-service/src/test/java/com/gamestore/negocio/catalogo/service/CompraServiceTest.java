package com.gamestore.negocio.catalogo.service;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.negocio.catalogo.model.Compra;
import com.gamestore.negocio.catalogo.model.Juego;
import com.gamestore.negocio.catalogo.repository.CompraRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    CompraRepository compraRepository;
    @Mock
    JuegoService juegoService;

    @InjectMocks
    CompraService compraService;

    private static final AuthPrincipal COMPRADOR = new AuthPrincipal(10, "comprador1", "COMPRADOR");

    private static Juego juego(long id) {
        Juego j = new Juego();
        j.setId(id);
        j.setNombre("Galaxy Raiders");
        j.setPrecio(new BigDecimal("39.99"));
        j.setImagenUrl("/uploads/gr.png");
        j.setVendedorUsername("vendedor1");
        return j;
    }

    @Test
    void comprar_juego_ya_comprado_tira_illegalArgument_y_no_descuenta_stock() {
        when(compraRepository.existsByJuegoIdAndCompradorUsername(1L, "comprador1")).thenReturn(true);

        assertThatThrownBy(() -> compraService.comprar(1L, COMPRADOR))
                .isInstanceOf(IllegalArgumentException.class);

        verify(juegoService, never()).descontarStock(anyLong());
    }

    @Test
    void comprar_ok_guarda_una_foto_del_juego() {
        when(compraRepository.existsByJuegoIdAndCompradorUsername(1L, "comprador1")).thenReturn(false);
        when(juegoService.descontarStock(1L)).thenReturn(juego(1L));
        when(compraRepository.save(any(Compra.class))).thenAnswer(inv -> inv.getArgument(0));

        compraService.comprar(1L, COMPRADOR);

        ArgumentCaptor<Compra> captor = ArgumentCaptor.forClass(Compra.class);
        verify(compraRepository).save(captor.capture());
        Compra guardada = captor.getValue();
        assertThat(guardada.getJuegoId()).isEqualTo(1L);
        assertThat(guardada.getNombreJuego()).isEqualTo("Galaxy Raiders");
        assertThat(guardada.getImagenUrl()).isEqualTo("/uploads/gr.png");
        assertThat(guardada.getPrecio()).isEqualByComparingTo("39.99");
        assertThat(guardada.getCompradorUsername()).isEqualTo("comprador1");
    }

    @Test
    void comprar_sin_stock_propaga_la_excepcion_y_no_crea_la_compra() {
        when(compraRepository.existsByJuegoIdAndCompradorUsername(1L, "comprador1")).thenReturn(false);
        when(juegoService.descontarStock(1L))
                .thenThrow(new IllegalArgumentException("No hay stock disponible de este juego"));

        assertThatThrownBy(() -> compraService.comprar(1L, COMPRADOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("stock");

        verify(compraRepository, never()).save(any());
    }

    @Test
    void juegosComprados_devuelve_los_ids_sin_duplicados() {
        Compra c1 = new Compra();
        c1.setJuegoId(1L);
        Compra c2 = new Compra();
        c2.setJuegoId(2L);
        Compra c3 = new Compra();
        c3.setJuegoId(1L);
        when(compraRepository.findByCompradorUsername("comprador1")).thenReturn(List.of(c1, c2, c3));

        assertThat(compraService.juegosComprados("comprador1")).containsExactlyInAnyOrder(1L, 2L);
    }
}
