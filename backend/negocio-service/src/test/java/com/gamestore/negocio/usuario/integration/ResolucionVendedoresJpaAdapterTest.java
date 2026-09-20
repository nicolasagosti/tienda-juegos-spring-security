package com.gamestore.negocio.usuario.integration;

import com.gamestore.negocio.catalogo.spi.ResolucionVendedores;
import com.gamestore.negocio.usuario.model.Rol;
import com.gamestore.negocio.usuario.model.Usuario;
import com.gamestore.negocio.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolucionVendedoresJpaAdapterTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    ResolucionVendedoresJpaAdapter adapter;

    @Test
    void porUsernames_vacio_no_toca_la_base() {
        assertThat(adapter.porUsernames(List.of())).isEmpty();
        assertThat(adapter.porUsernames(null)).isEmpty();
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void porUsernames_indexa_por_username_y_expone_el_rol_como_texto() {
        Usuario v1 = new Usuario("vendedor1", "Nintenrog Games", "v1@x.com", Rol.VENDEDOR);
        when(usuarioRepository.findByUsernameIn(any())).thenReturn(List.of(v1));

        Map<String, ResolucionVendedores.Vendedor> mapa = adapter.porUsernames(List.of("vendedor1", "vendedor1"));

        assertThat(mapa).containsOnlyKeys("vendedor1");
        ResolucionVendedores.Vendedor v = mapa.get("vendedor1");
        assertThat(v.nombreCompleto()).isEqualTo("Nintenrog Games");
        assertThat(v.rol()).isEqualTo("VENDEDOR");
        assertThat(v.habilitado()).isTrue();
    }
}
