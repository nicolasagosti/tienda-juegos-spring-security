package com.gamestore.negocio.catalogo.service;

import com.gamestore.negocio.catalogo.model.Seccion;
import com.gamestore.negocio.catalogo.repository.SeccionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeccionServiceTest {

    @Mock
    SeccionRepository seccionRepository;

    @InjectMocks
    SeccionService seccionService;

    @Test
    void crear_nombre_repetido_tira_illegalArgument_y_no_guarda() {
        when(seccionRepository.existsByNombreIgnoreCase("RPG")).thenReturn(true);

        assertThatThrownBy(() -> seccionService.crear("RPG", "algo"))
                .isInstanceOf(IllegalArgumentException.class);

        verify(seccionRepository, never()).save(any());
    }

    @Test
    void crear_nombre_nuevo_persiste_la_seccion() {
        when(seccionRepository.existsByNombreIgnoreCase("Indie")).thenReturn(false);
        when(seccionRepository.save(any(Seccion.class))).thenAnswer(inv -> inv.getArgument(0));

        seccionService.crear("Indie", "Juegos independientes");

        ArgumentCaptor<Seccion> captor = ArgumentCaptor.forClass(Seccion.class);
        verify(seccionRepository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Indie");
        assertThat(captor.getValue().getDescripcion()).isEqualTo("Juegos independientes");
    }

    @Test
    void eliminar_delega_en_el_repo() {
        seccionService.eliminar(7L);

        verify(seccionRepository).deleteById(7L);
    }
}
