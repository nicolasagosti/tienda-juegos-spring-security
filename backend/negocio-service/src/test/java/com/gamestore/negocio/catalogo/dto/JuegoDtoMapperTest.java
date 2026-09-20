package com.gamestore.negocio.catalogo.dto;

import com.gamestore.common.security.AuthPrincipal;
import com.gamestore.negocio.catalogo.model.Juego;
import com.gamestore.negocio.catalogo.service.CompraService;
import com.gamestore.negocio.catalogo.service.JuegoService;
import com.gamestore.negocio.catalogo.spi.ResolucionVendedores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JuegoDtoMapperTest {

    @Mock
    ResolucionVendedores resolucionVendedores;
    @Mock
    JuegoService juegoService;
    @Mock
    CompraService compraService;

    @InjectMocks
    JuegoDtoMapper mapper;

    private static Juego juego(long id, String vendedor) {
        Juego j = new Juego();
        j.setId(id);
        j.setNombre("Juego " + id);
        j.setPrecio(new BigDecimal("10.00"));
        j.setStock(1);
        j.setVendedorUsername(vendedor);
        return j;
    }

    @Test
    void aDtos_resuelve_vendedores_en_una_sola_llamada_y_marca_comprados_para_comprador() {
        AuthPrincipal comprador = new AuthPrincipal(1, "comprador1", "COMPRADOR");
        Juego j1 = juego(1L, "vendedor1");
        Juego j2 = juego(2L, "vendedor1");
        when(resolucionVendedores.porUsernames(any())).thenReturn(Map.of(
                "vendedor1", new ResolucionVendedores.Vendedor(9L, "vendedor1", "Nintenrog Games",
                        "v1@x.com", "VENDEDOR", true)));
        when(compraService.juegosComprados("comprador1")).thenReturn(Set.of(1L));
        when(juegoService.puedeGestionar(any(), any())).thenReturn(false);

        List<JuegoDto> dtos = mapper.aDtos(List.of(j1, j2), comprador);

        verify(resolucionVendedores).porUsernames(List.of("vendedor1"));
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).vendedor().nombreCompleto()).isEqualTo("Nintenrog Games");
        assertThat(dtos.get(0).comprado()).isTrue();
        assertThat(dtos.get(1).comprado()).isFalse();
    }

    @Test
    void aDtos_usa_vendedor_degradado_cuando_no_hay_perfil() {
        AuthPrincipal vendedor = new AuthPrincipal(2, "vendedor1", "VENDEDOR");
        when(resolucionVendedores.porUsernames(any())).thenReturn(Map.of());
        when(juegoService.puedeGestionar(any(), any())).thenReturn(true);

        List<JuegoDto> dtos = mapper.aDtos(List.of(juego(1L, "fantasma")), vendedor);

        assertThat(dtos.get(0).vendedor().username()).isEqualTo("fantasma");
        assertThat(dtos.get(0).vendedor().nombreCompleto()).isEqualTo("fantasma");
        // Un VENDEDOR no "compra": no se consulta el historial de compras.
        verify(compraService, never()).juegosComprados(any());
    }
}
