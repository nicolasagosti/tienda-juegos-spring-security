package com.gamestore.negocio.catalogo.integration;

import com.gamestore.negocio.catalogo.repository.JuegoRepository;
import com.gamestore.negocio.catalogo.repository.SeccionRepository;
import com.gamestore.negocio.usuario.spi.ConsultaCatalogo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultaCatalogoJpaAdapterTest {

    @Mock
    JuegoRepository juegoRepository;
    @Mock
    SeccionRepository seccionRepository;

    @InjectMocks
    ConsultaCatalogoJpaAdapter adapter;

    @Test
    void totales_suma_juegos_y_secciones() {
        when(juegoRepository.count()).thenReturn(13L);
        when(seccionRepository.count()).thenReturn(5L);

        ConsultaCatalogo.Totales t = adapter.totales();

        assertThat(t.totalJuegos()).isEqualTo(13L);
        assertThat(t.totalSecciones()).isEqualTo(5L);
    }

    @Test
    void juegosDeVendedor_delega_en_el_repo() {
        when(juegoRepository.countByVendedorUsername("vendedor1")).thenReturn(4L);

        assertThat(adapter.juegosDeVendedor("vendedor1")).isEqualTo(4L);
    }
}
